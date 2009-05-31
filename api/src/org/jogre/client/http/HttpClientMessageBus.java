package org.jogre.client.http;

import java.util.ConcurrentModificationException;
import java.util.Properties;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jogre.common.PayloadBuilder;
import org.jogre.common.TransmissionException;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import org.jogre.common.MessageBus;
import org.jogre.common.comm.ITransmittable;
import org.jogre.common.util.JogreLogger;

/**
 * A MessageBus implementation that can be used on a Java based client.
 * 
 * @author Jens Scheffler
 *
 */
public class HttpClientMessageBus implements MessageBus {
  
  /**
   * Abstraction of everything that depends on the runtime environment,
   * like system clock, threading, or network. Easy to replace with
   * mocks for unit tests.
   */
  public static interface Environment {
    
    /**
     * Performs an http request to the server
     * @param data the data to be transmitted
     * @return the response payload, or null if the connection failed.
     */
    public String fetch(String data);
    
    /**
     * Controls the execution of the message bus' run-method in an independent
     * thread. Similar to the Executor interface, just not for generic runnables,
     * and it would also work in Java 1.3
     */
    public void execute(HttpClientMessageBus bus);
    
    /**
     * Holds the current thread for a certain amount of milliseconds
     */
    public void sleep(long millis) throws InterruptedException;
    
    /**
     * Gets the current time in milliseconds
     */
    public long currentTimeMillis();
    
  }
  
  private final Queue<String> outqueue = new ConcurrentLinkedQueue<String>();
  private final Properties props = new Properties();
  private final JogreLogger logger = new JogreLogger(HttpClientMessageBus.class);
  private final Environment env;
  private final int silencePeriodInMillis;
  private final int maxMessages;
  private boolean isInterrupted = false;
  private boolean isStarted = false;
  private MessageParser parser;

  /**
   * Constructor
   * @param env the Environment that this message bus runs in
   * @param silencePeriodInMillis the time the thread should wait between each http call
   * @param maxMessages the maximum amount of messages that should be transported in one http request
   */
  public HttpClientMessageBus(
      Environment env,
      int silencePeriodInMillis,
      int maxMessages) {
    super();
    this.env = env;
    this.maxMessages = maxMessages;
    this.silencePeriodInMillis = silencePeriodInMillis;
  }
  
  /**
   * Checks if the interrupted-flag is set (using synchronization on this object)
   */
  private synchronized void checkForInterruption() throws InterruptedException {
    if (isInterrupted) {
      throw new InterruptedException();
    }
  }
  
  /**
   * Sleeps for a while.
   */
  private void sleep(long lastComm) throws InterruptedException {
    long diff = env.currentTimeMillis() - lastComm - silencePeriodInMillis;
    if (diff > 0) {
      env.sleep(diff);
    }
  }
  
  /**
   * Signals a running executing thread to cease its work.
   */
  @Override
  public synchronized void close() {
    isInterrupted = true;
  }
  
  /**
   * Starts a thread (through the executor) that connects to the server on a regular base
   */
  @Override
  public synchronized void open(MessageParser parser) {
    if (isStarted) {
      throw new ConcurrentModificationException("Cannot call open more than once!");
    }
    isStarted = true;
    this.parser = parser;
    env.execute(this);
  }

  /**
   * Enqueues an object for transmission.
   */
  @Override
  public void send(ITransmittable transObject) {
    outqueue.add(transObject.flatten().toString());
  }

  /**
   * Stores a local connection property.
   */
  @Override
  public void setProperty(String key, String valueOrNull) {
    if (valueOrNull == null) {
      props.remove(key);
    } else {
      props.setProperty(key, valueOrNull);
    }
  }

  /**
   * Retrieves a local connection property.
   */
  @Override
  public String getProperty(String key, String defaultValue) {
    return props.getProperty(key, defaultValue);
  }
  
  /**
   * For unit tests, retrieve the queue that internally stores the XML snippets.
   */
  Queue<String> getQueue() {
    return outqueue;
  }

  /**
   * Performs the communication loop. Visible for unit tests and the "Environment" implementation.
   */
  void run() throws InterruptedException {
    
    // Some tool variables we will need throughout this method
    final PayloadBuilder payload = new PayloadBuilder(null);   // constructs xml messages
    Object lastMeta = null;  // The last "meta"-tag that was sent from the server
    long lastCommunication = 0;  // the last time we tried to contact the server
    XMLElement elem = new XMLElement(); // an element we can parse feedback into
    
    // Initial connection: an empty payload is sent. All we expect the response to be
    // at this point is parseable xml with a meta-element that we can begin conversation with
    while(lastMeta == null) {
      checkForInterruption();
      lastCommunication = env.currentTimeMillis();
      String hello = env.fetch(payload.toString());
      if (hello != null) {
        try {
          elem.parseString(hello);
          lastMeta = elem.getAttribute(PayloadBuilder.META);
        } catch(XMLParseException parseFailed) {
          logger.error("Could not parse http response", parseFailed.getMessage());
        }
      }
      if (lastMeta == null) {
        sleep(lastCommunication);
      }
    }
    payload.reset(lastMeta.toString());
    
    // Payload exchange
    while (true) {
      
      // First, check if we should terminate, or at least sleep a little while
      checkForInterruption();
      sleep(lastCommunication);
      
      // Fill the payload
      while (payload.size() < maxMessages && !outqueue.isEmpty()) {
        payload.addChildNoparse(outqueue.poll());       
      }
      
      // Submit the data as a request and evaluate the response
      lastCommunication = env.currentTimeMillis();
      final String response = env.fetch(payload.toString());
      if (response != null) {
        
        // Parse the xml element and extract the last meta. If there is
        // no meta, something went wrong, and we should try again later
        try {
          elem.parseString(response);
          final Object newMeta = elem.getAttribute(PayloadBuilder.META);
          if (lastMeta == null) {
            continue;
          } else {
            lastMeta = newMeta;
          }
        } catch(XMLParseException parseFailed) {
          logger.error("Could not parse http response", parseFailed.getMessage());
        }
        
        // Iterate through the children and submit them to the parser. If a message fails,
        // we log the issue but continue
        for (XMLElement message : (Vector<XMLElement>) (elem.getChildren())) {
          checkForInterruption();
          try {
            parser.parse(message);
          } catch (TransmissionException e) {
            logger.error("Transmission Exception for :" + message.toString(), e.getMessage());
          } catch (RuntimeException e) {
            logger.error("Runtime Exception for :" + message.toString(), e.getMessage());
          } catch (Error e) {
            logger.error("Runtime Error for :" + message.toString(), e.getMessage());
          }
        }
        
        // Now that we successfully transmitted the messages, let's reset the buffer to make space
        // for new messages from the queue
        payload.reset(lastMeta.toString());
      }
    }
  }

}
