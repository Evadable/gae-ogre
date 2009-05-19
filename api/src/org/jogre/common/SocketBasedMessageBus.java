package org.jogre.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import org.jogre.common.comm.ITransmittable;
import org.jogre.common.util.JogreLogger;

/**
 * Represents client/server communication over a socket object
 * @author jens
 *
 */
public class SocketBasedMessageBus {
  
  /** Logging */
  private final JogreLogger logger = new JogreLogger (this.getClass());
  
  /** When the boolean loop becomes false the Thread finishes. */
  protected boolean loop = true;

  private final BufferedReader in;
  private final PrintStream out;
  private final Socket socket;
  
  private final AbstractConnectionThread thread;
  
  private Thread executingThread;
  
  public SocketBasedMessageBus(
      final AbstractConnectionThread thread, final Socket socket) throws IOException {
    if (thread != null) {
      this.thread = thread;
    } else {
      throw new NullPointerException("thread must not be null");
    }
    if (socket != null) {
      this.socket = socket;
      in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
      out = new PrintStream (socket.getOutputStream());
    } else {
      throw new NullPointerException("thread must not be null");
    }
  }
  
  private void doLoop() {
    logger.debug("run", "Staring thread.");

    try {
      while (loop) {
        // listen for input from the user
        String inString = "";

        while (loop && inString!=null && inString.equals("")) {
          if (in == null) {
            thread.cleanup ();
            return;
          }
          inString = in.readLine();
        }

        // Check input.
        if (in == null) {
          thread.cleanup ();
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
                  thread.parse(message);
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
      thread.cleanup ();
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

  /**
   * Send a ITransmittable object to the output stream (could be server or
   * client).
   *
   * @param transObject
   */
  public void send (ITransmittable transObject) {
    // Retrieve XMLElement from the object and flatten to a String.
    String message = transObject.flatten().toString();

    // Send down the socket to the receiving end
    out.println (message);
  }
  
  /**
   * Stop the loop.
   */
  public void stopLoop () {
    this.loop = false;
  }
  
  /**
   * Starts the loop. May not be called more than once.
   */
  public void startLoop() {
    if (executingThread != null) {
      throw new AssertionError("Cannot call startLoop more than once!");
    }
    executingThread = new Thread() {
      @Override
      public void run() {
        doLoop();
      }
    };
    executingThread.start();
  }
}
