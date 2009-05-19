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
  JogreLogger logger = new JogreLogger (this.getClass());
  
  /** When the boolean loop becomes false the Thread finishes. */
  protected boolean loop = true;

  /** All clients start initially with "connected" equal to false
   *  (although they can still recieve/transfer logon information). */
  protected boolean connected = false;

  private BufferedReader in;
  private PrintStream out;
  
  public SocketBasedMessageBus(final Socket socket) throws IOException {
    if (socket != null) {
      in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
      out = new PrintStream (socket.getOutputStream());
    }
  }
  
  public void run(final AbstractConnectionThread thread) {
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
    catch (Exception genEx) {
      genEx.printStackTrace();
      logger.error ("run", "General Exception: ");
      logger.stacktrace (genEx);      
    }

    connected = false;
    thread.cleanup ();
    
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
   * Set boolean to specify that this client has connected sucessfully.
   */
  public void connect () {
    this.connected = true;
  }
  
  /**
   * Stop the loop.
   */
  public void stopLoop () {
    //TODO: shouldn't we close the streams (or even better, the socket) here?
    this.loop = false;
  }
}
