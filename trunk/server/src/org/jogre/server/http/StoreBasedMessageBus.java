package org.jogre.server.http;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Properties;

import org.jogre.common.MessageBus;
import org.jogre.common.comm.ITransmittable;
import org.jogre.server.data.ProtoSchema.MessageBusState;
import org.jogre.server.data.ProtoSchema.OutgoingMessage;
import org.jogre.server.data.ProtoSchema.Property;

import com.appenginefan.toolkit.persistence.Persistence;
import com.appenginefan.toolkit.persistence.ProtocolBufferPersistence;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * A MessageBus class that uses a Peristence store as backend.
 * 
 * @author Jens Scheffler
 *
 */
class StoreBasedMessageBus implements MessageBus {
  
  private final Persistence<MessageBusState> store;
  
  private String key;
  private Properties cachedProperties;
  private List<String> unsavedMessages = Lists.newArrayList();
  private String cachedSecret;
  private int highestAckedMessage = -1;
  private boolean isModified;
  private MessageBusState lastKnownState = null;
  
  String getRandomKey() {
    return String.valueOf(Math.random()).substring(2);
  }
  
  StoreBasedMessageBus(String key, Persistence<byte[]> binaryStore) {
    Preconditions.checkNotNull(binaryStore);
    this.key = key;
    store = new ProtocolBufferPersistence<MessageBusState>(
        binaryStore, MessageBusState.getDefaultInstance());
  }
  
  /**
   * Make sure that the bus is open and not deleted from the store
   */
  private void checkOpen() {
    if (key == null) {
      throw new ConcurrentModificationException("bus is not open");
    }
    if (cachedProperties == null) {
      lastKnownState = store.get(key);
      if (lastKnownState == null) {
        throw new ConcurrentModificationException("bus has already been closed");
      }
      isModified = false;
      cachedSecret = lastKnownState.getRandomSecret();
      cachedProperties = new Properties();
      for (Property p : lastKnownState.getConnectionPropertiesList()) {
        cachedProperties.setProperty(p.getKey(), p.getValue());
      }
    }
  }

  @Override
  public void close() {
    checkOpen();
    store.mutate(key, Functions.constant((MessageBusState) null));
    isModified = false;
    cachedProperties = null;
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    checkOpen();
    return cachedProperties.getProperty(key, defaultValue);
  }

  @Override
  public void open(MessageParser parser) {
    
    // Already open?
    if (key != null) {
      throw new ConcurrentModificationException("Cannot open twice");
    }
    
    // Try to create a new entry in the store
    isModified = false;
    while(key == null) {
      
      // Create random key and try to insert
      key = getRandomKey();
      store.mutate(key, new Function<MessageBusState, MessageBusState>(){
        @Override
        public MessageBusState apply(MessageBusState oldState) {
          
          // If oldState exists, that means there was a key collision
          if (oldState != null) {
            key = null;
            return oldState;
          }
          
          // No collision, so store a new empty object
          cachedSecret = String.valueOf(Math.random());
          return lastKnownState = MessageBusState
                                  .newBuilder()
                                  .setRandomSecret(cachedSecret)
                                  .build();
        }});
    }
  }

  @Override
  public void send(ITransmittable transObject) {
    checkOpen();
    isModified = true;
    unsavedMessages.add(transObject.flatten().toString());
  }

  @Override
  public void setProperty(String key, String valueOrNull) {
    checkOpen();
    isModified = true;
    if (valueOrNull != null) {
      cachedProperties.setProperty(key, valueOrNull);
    } else {
      cachedProperties.remove(valueOrNull);
    }
  }

  void save() {
    checkOpen();
    if (!isModified) {
      return;
    }
    store.mutate(key, new Function<MessageBusState, MessageBusState>(){
      @Override
      public MessageBusState apply(MessageBusState oldState) {
        
        // If oldState exists, that means there was a deletion in-between
        if (oldState == null) {
          key = null;
          return oldState;
        }
        
        // Build a new object from the old one
        MessageBusState.Builder builder = MessageBusState.newBuilder(oldState);
        
        // Overwrite all connection properties
        builder.clearConnectionProperties();
        for (Object prop : cachedProperties.keySet()) {
          builder.addConnectionProperties(
              Property.newBuilder()
              .setKey((String) prop)
              .setValue(cachedProperties.getProperty((String) prop))
              .build());
        }
        
        // Remove any acked message
        int maxId = Math.max(highestAckedMessage, 1);
        builder.clearMessageQueue();
        for (int i = oldState.getMessageQueueCount() - 1; i >= 0; i--) {
          OutgoingMessage messageInQueue = oldState.getMessageQueue(i);
          maxId = Math.max(maxId, messageInQueue.getAckToken());
          if (messageInQueue.getAckToken() > highestAckedMessage) {
            builder.addMessageQueue(messageInQueue);
          }
        }
        
        // Attach any cached messages
        if (!unsavedMessages.isEmpty()) {
          maxId++;
          for (String payload : unsavedMessages) {
            builder.addMessageQueue(
                OutgoingMessage.newBuilder()
                .setAckToken(maxId)
                .setPayload(payload)
                .build());
          }
        }
        
        // Done
        return lastKnownState = builder.build();
      }});
    
    // If there were any unsaved messages and we arrived at this point,
    // they should be persisted
    unsavedMessages.clear();
  }
  
  String getHandle() {
    checkOpen();
    return key;
  }
  
  String getSecret() {
    checkOpen();
    return cachedSecret;
  }
  
  void ack(int lastAckTocken) {
    checkOpen();
    isModified = true;
    highestAckedMessage = Math.max(highestAckedMessage, lastAckTocken);
  }
  
  MessageBusState getLastKnownState() {
    return lastKnownState;
  }
}
