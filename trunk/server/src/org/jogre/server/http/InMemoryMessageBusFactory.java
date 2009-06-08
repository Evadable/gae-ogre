package org.jogre.server.http;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jogre.common.MessageBus;
import org.jogre.common.PayloadBuilder;
import org.jogre.common.comm.ITransmittable;

/**
 * A first in-memory implementation of the MessageBusFactory, to be used
 * for the non-appengine version of Jogre
 * 
 * @author Jens Scheffler
 *
 */
public class InMemoryMessageBusFactory implements MessageBusFactory {
  
  private int counter = 0;
  
  private Map<String, NaiiveMessageBus> knownBusses = 
      Collections.synchronizedMap(new HashMap<String, NaiiveMessageBus>());

  private synchronized int newCount() {
    return ++counter;
  }
  
  @Override
  public MessageBus fromHandle(String handle) {
    return knownBusses.get(handle);
  }

  @Override
  public MessageBus loadMessageBus(
      HttpServletRequest request, String meta) {
    
    // Null or empty meta?
    if (meta == null || meta.isEmpty()) {
      return null;
    }
    
    // In this implementation, the meta contains two values:
    // the id of the bus, and the last message processed
    // This is a naiive implementation, and we do not account
    // for people trying to hack the game by manipulating the meta
    String[] items = meta.split(",");
    if (items.length != 2) {
      return null;
    }
    try {
      Integer.parseInt(items[0]);
      Integer.parseInt(items[1]);
    } catch (NumberFormatException e) {
      return null;
    }
    
    // Do we know the bus?
    NaiiveMessageBus bus = knownBusses.get(items[0]);
    if (bus == null) {
      return null;
    }
    
    // Clear out any messages that might be handled
    final int lastId = Integer.parseInt(items[1]);
    while (!bus.snippets.isEmpty()) {
      if (bus.snippets.get(0).id <= lastId) {
        bus.snippets.remove(0);
      } else {
        break;
      }
    }
    
    // Done
    return bus;
  }

  @Override
  public MessageBus newMessageBus(HttpServletRequest request) {
    return new NaiiveMessageBus();
  }

  @Override
  public String toHandle(MessageBus bus) {
    return ((NaiiveMessageBus) bus).id;
  }

  @Override
  public void writeState(MessageBus bus,
      HttpServletResponse response) throws IOException {
    final NaiiveMessageBus busImpl = (NaiiveMessageBus) bus;
    final int newState = newCount();
    PayloadBuilder payload = PayloadBuilder.payload(busImpl.id + "," + newState);
    synchronized (busImpl.snippets) {
      for (QueuedMessage snippet : busImpl.snippets) {
        snippet.id = newState;
        payload.addChildNoparse(snippet.snippet);
      }
    }
    response.getOutputStream().println(payload.toString());
  }
  
  private static class QueuedMessage {
    String snippet;
    int id = Integer.MAX_VALUE;
    public QueuedMessage(String snippet) {
      this.snippet = snippet;
    }
  }
  
  private class NaiiveMessageBus implements MessageBus {
    
    private List<QueuedMessage> snippets = 
        Collections.synchronizedList(new LinkedList<QueuedMessage>());
    private String id;
    private Properties props = new Properties();

    public NaiiveMessageBus() {
      id = "" + newCount();
    }
    
    @Override
    public void close() {
      snippets.clear();
      knownBusses.remove(id);
    }

    @Override
    public String getProperty(String key,
        String defaultValue) {
      return props.getProperty(key, defaultValue);
    }

    @Override
    public void open(MessageParser parser) {
      knownBusses.put(id, this);
    }

    @Override
    public void send(ITransmittable transObject) {
      snippets.add(new QueuedMessage(transObject.flatten().toString()));
      
    }

    @Override
    public void setProperty(String key, String valueOrNull) {
      if (valueOrNull != null) {
        props.setProperty(key, valueOrNull);
      } else {
        props.remove(key);
      }
    }
  }
}
