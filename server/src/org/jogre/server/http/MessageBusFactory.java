package org.jogre.server.http;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jogre.common.MessageBus;

/**
 * Converts MessageBus objects into simple string handles and vice versa.
 * These strings will be stored on the client side as a cookie and are used to
 * uniquely identify the connection. Since the handles are unique, they may also be
 * used on the server side to persist the inner state of the connection.
 *  
 * @author Jens Scheffler
 *
 */
public interface MessageBusFactory {
  
  /**
   * For the given HttpServletRequest, create a new message bus.
   * This is the equivalent of a newly opened socket connection.
   * @param request the request that triggers the creation
   * @return a non-null MessageBus object that is not "open" yet
   */
  public MessageBus newMessageBus(HttpServletRequest request);
  
  /**
   * Assumed a given request belongs to an existing MessageBus, loads
   * that object. It is assumed that the MessageBus is in its connected 
   * state. Unlike newMessageBus(), an existing MessageBus object may have
   * additional state in a database. This method is expected to load
   * that data, as well.
   * @param request the request that triggers the lookup
   * @param the meta-tag from the http request
   * @return a MessageBus object if existent, or null if nothing is found
   */
  public MessageBus loadMessageBus(HttpServletRequest request, String meta);
  
  /**
   * Populate the cookies in a servlet response with whatever data is needed
   * to support the loadMessageBus() method in future requests. Also, write the
   * body of the response.
   * @param bus the MessageBus to transmit
   * @param response the response to populate
   */
  public void writeState(MessageBus bus, HttpServletResponse response) throws IOException;
  
  /**
   * Converts a MessageBus to a unique handle that can be used to persist
   * inner state in a data store
   * @param bus a non-null MessageBus
   * @return the unique id of the bus
   */
  public String toHandle(MessageBus bus);
  
  /**
   * Loads a MessageBus object from a given handle. The MessageBus object is
   * usually less "complete" than an object constructed from a request, since it
   * does not contain the state stored in the http client, but it is sufficient
   * to perform certain manipulattions, like enqueuing outgoing messages.
   * @param handle the handle that should be used for constructing the object
   * @return a MessageBus object, or null if the handle is invalid
   */
  public MessageBus fromHandle(String handle);
  
  /**
   * Commits any changes made to the store in this thread that have not been written yet.
   */
  public void commit(HttpServletRequest request);
  
  /**
   * Commits any changes made to the store in this thread that have not been written yet.
   */
  public void rollback(HttpServletRequest request);
}
