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
public class StoreBasedMessageBus implements MessageBus {
  
  private final Persistence<MessageBusState> store;
  
  private String key;
  private Properties cachedProperties;
  private List<String> unsavedMessages = Lists.newArrayList();
  
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
      MessageBusState state = store.get(key);
      if (state == null) {
        throw new ConcurrentModificationException("bus has already been closed");
      }
      cachedProperties = new Properties();
      for (Property p : state.getConnectionPropertiesList()) {
        cachedProperties.setProperty(p.getKey(), p.getValue());
      }
    }
  }

  @Override
  public void close() {
    checkOpen();
    store.mutate(key, Functions.constant((MessageBusState) null));
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
          return MessageBusState.newBuilder().build();
        }});
    }
  }

  @Override
  public void send(ITransmittable transObject) {
    checkOpen();
    unsavedMessages.add(transObject.flatten().toString());
  }

  @Override
  public void setProperty(String key, String valueOrNull) {
    checkOpen();
    if (valueOrNull != null) {
      cachedProperties.setProperty(key, valueOrNull);
    } else {
      cachedProperties.remove(valueOrNull);
    }
  }

  void save() {
    checkOpen();
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
        
        // Attach any cached messages
        if (!unsavedMessages.isEmpty()) {
          for (String payload : unsavedMessages) {
            builder.addMessageQueue(
                OutgoingMessage.newBuilder()
                .setAckToken(-1)
                .setPayload(payload)
                .build());
          }
        }
        
        // Done
        return builder.build();
      }});
    
    // If there were any unsaved messages and we arrived at this point,
    // they should be persisted
    unsavedMessages.clear();
  }
}
