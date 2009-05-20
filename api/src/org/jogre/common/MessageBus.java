package org.jogre.common;

import java.util.ConcurrentModificationException;

import org.jogre.common.comm.ITransmittable;

/**
 * Represents client/server communication over an arbitrary low-level protocol,
 * such as sockets or http 
 */
public interface MessageBus {

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
   * Connects the message bus with an AbstractConnectionThread.
   * If the MessageBus implementation requires to start any internal
   * threading, this is where that action is kicked off.
   * @exception ConcurrentModificationException if the method is called more than once
   */
  public abstract void open(
      final AbstractConnectionThread thread);

}