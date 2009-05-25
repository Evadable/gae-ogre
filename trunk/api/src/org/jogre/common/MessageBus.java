package org.jogre.common;

import java.util.ConcurrentModificationException;

import nanoxml.XMLElement;

import org.jogre.common.comm.ITransmittable;

/**
 * Represents client/server communication over an arbitrary low-level protocol,
 * such as sockets or http 
 */
public interface MessageBus {
  
  /**
   * Represents logic that knows how to deal with incoming messages.
   */
  public static interface MessageParser {
    
    /**
     * Handles an incoming snippet of XML data
     *
     * @param message       Communication as an XML object.
     * @throws TransmissionException  This is thrown if there is a problem parsing the String.
     */
    public abstract void parse (XMLElement message) throws TransmissionException;
    
    /**
     * This method is called to properly clean up after a client.
     */
    public abstract void cleanup ();

  }

  /**
   * Send a ITransmittable object to the outgoing channel of this
   * communication. Depending on the concrete implementation, this can
   * be synchronous or asynchronous.
   *
   * @param transObject the object that should be sent out.
   */
  public abstract void send(ITransmittable transObject);

  /**
   * Signals that the communication via this channel is over.
   * Initiates any cleanup that might be necessary. Once this has happened,
   * the object becomes unusable -- it is undefined how calls to it would
   * behave!
   */
  public abstract void close();

  /**
   * Connects the message bus with a MessageParser and initiates communication.
   * If the MessageBus implementation requires to start any internal
   * threading, this is where that action is kicked off.
   * @exception ConcurrentModificationException if the method is called more than once
   */
  public abstract void open(final MessageParser parser);
  
  /**
   * Set a property that should be associated with the MessageBus.
   * Connection objects that use the messaging object can be non-durable, so they need
   * a way to store a small amount of data for bootstrapping. In a http-based world,
   * the messaging implementation may choose to set the data as part of an http cookie.
   */
  public abstract void setProperty(String key, String valueOrNull);
  
  /**
   * Gets a property currently associated with this object.
   */
  public abstract String getProperty(String key, String defaultValue);

}