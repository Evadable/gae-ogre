package org.jogre.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ConcurrentModificationException;
import java.util.Properties;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import org.jogre.common.comm.ITransmittable;
import org.jogre.common.util.JogreLogger;

/**
 * Represents client/server communication over a socket object
 * @author jens
 *
 */
public class SocketBasedMessageBus implements MessageBus {
  
  /** Logging */
  private final JogreLogger logger = new JogreLogger (this.getClass());
  
  /** When the boolean loop becomes false the Thread finishes. */
  private boolean loop = true;
  
  /** Properties associated with this object */
  private final Properties properties;

  private final BufferedReader in;
  private final PrintStream out;
  private final Socket socket;
  
  private Thread executingThread;
  
  public SocketBasedMessageBus(final Socket socket) {
    try {
      if (socket != null) {
        this.socket = socket;
        in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
        out = new PrintStream (socket.getOutputStream());
      } else {
        throw new NullPointerException("thread must not be null");
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not connect to socket", e);
    }
    properties = new Properties();
  }
  
  private void doLoop(final MessageParser parser) {
    logger.debug("run", "Staring thread.");

    try {
      while (loop) {
        // listen for input from the user
        String inString = "";

        while (loop && inString!=null && inString.equals("")) {
          if (in == null) {
            parser.cleanup ();
            return;
          }
          inString = in.readLine();
        }

        // Check input.
        if (in == null) {
          parser.cleanup ();
          return;
        }

        // Ensure that the communication is XML (starts with a '<' character)
        // as any sort of client could send communication to the server.
        if (inString != null) {
          if (inString.startsWith("<")) {
  
            // Starts with an '<' so try and parse this XML
            XMLElement message = new XMLElement ();
  
            try {
                message.parseString (inString);
  
                // parse this element
              if (message != null)
                parser.parse(message);
            }
            catch (XMLParseException xmlParseEx) {
              logger.error ("run", "problem parsing: " + inString);
              logger.stacktrace (xmlParseEx);
            }
          }
        }
      }
    }
    catch (SocketException sEx) {
      logger.debug ("run", "Connection lost");
      logger.stacktrace (sEx);
    }
    catch (IOException ioEx) {
      logger.error ("run", "IO Exception: ");
      logger.stacktrace (ioEx);
    }
    catch (Throwable genEx) {
      genEx.printStackTrace();
      logger.error ("run", "General Exception: ");
      logger.stacktrace (genEx);      
    }
    try {
      parser.cleanup ();
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
        logger.error ("run", "Could not close socket: ");
        logger.stacktrace (e);      
      }
    }
  }

  /* (non-Javadoc)
   * @see org.jogre.common.MessageBus#send(org.jogre.common.comm.ITransmittable)
   */
  public void send (ITransmittable transObject) {
    // Retrieve XMLElement from the object and flatten to a String.
    String message = transObject.flatten().toString();

    // Send down the socket to the receiving end
    out.println (message);
  }
  
  /* (non-Javadoc)
   * @see org.jogre.common.MessageBus#close()
   */
  public void close () {
    this.loop = false;
  }
  
  /* (non-Javadoc)
   * @see org.jogre.common.MessageBus#open(org.jogre.common.AbstractConnectionThread)
   */
  public void open(final MessageParser parser) {
    if (parser == null) {
      throw new NullPointerException("parser must not be null");
    }
    if (executingThread != null) {
      throw new ConcurrentModificationException("Cannot call startLoop more than once!");
    }
    executingThread = new Thread() {
      @Override
      public void run() {
        doLoop(parser);
      }
    };
    executingThread.start();
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    return properties.getProperty(key, defaultValue);
  }

  @Override
  public void setProperty(String key, String valueOrNull) {
    if (valueOrNull == null) {
      properties.remove(key);
    } else {
      properties.setProperty(key, valueOrNull);
    }
  }
}
