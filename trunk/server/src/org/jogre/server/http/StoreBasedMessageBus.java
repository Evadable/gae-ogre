package org.jogre.server.http;

import java.util.Properties;

import org.jogre.common.MessageBus;
import org.jogre.common.comm.ITransmittable;
import org.jogre.server.data.ProtoSchema.MessageBusState;

import com.appenginefan.toolkit.persistence.Persistence;
import com.appenginefan.toolkit.persistence.ProtocolBufferPersistence;
import com.google.appengine.repackaged.com.google.common.base.Preconditions;

/**
 * A MessageBus class that uses a Peristence store as backend.
 * 
 * @author Jens Scheffler
 *
 */
public class StoreBasedMessageBus implements MessageBus {
  
  private String key;
  private final Persistence<MessageBusState> store;
  private final Properties cachedProperties = new Properties();
  private boolean mayCacheMessages;
  
  StoreBasedMessageBus(String key, Persistence<byte[]> binaryStore) {
    Preconditions.checkNotNull(binaryStore);
    this.key = key;
    store = new ProtocolBufferPersistence<MessageBusState>(
        binaryStore, MessageBusState.getDefaultInstance());
  }

  @Override
  public void close() {
    // TODO: verify that the key is not null and the bus is not deleted
    // TODO: delete from store and make sure that nothing ever gets persisted again
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    return cachedProperties.getProperty(key, defaultValue);
  }

  @Override
  public void open(MessageParser parser) {
    // TODO: verify that the key is null
    // TODO: pick random key and make sure it is not taken yet
    // TODO: store an empty element in the database for the random key
  }

  @Override
  public void send(ITransmittable transObject) {
    // TODO: verify that the key is not null and the bus is not deleted
    // TODO: how do we know whether to cache or persist immediately?
  }

  @Override
  public void setProperty(String key, String valueOrNull) {
    // TODO: verify that the key is not null and the bus is not deleted
    // TODO: how do we know whether to cache or persist immediately?
  }

  void save() {
    // TODO: verify that the key is not null and the bus is not deleted
    // TODO: only call if "mayCacheMessages"
  }
}
