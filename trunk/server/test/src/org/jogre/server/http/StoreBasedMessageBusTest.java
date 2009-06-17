package org.jogre.server.http;

import java.util.ConcurrentModificationException;

import org.easymock.EasyMock;
import org.jogre.common.MessageBus.MessageParser;
import org.jogre.common.comm.CommError;
import org.jogre.server.data.ProtoSchema.MessageBusState;
import org.jogre.server.data.ProtoSchema.OutgoingMessage;

import com.appenginefan.toolkit.persistence.MapBasedPersistence;
import com.appenginefan.toolkit.persistence.Persistence;
import com.appenginefan.toolkit.persistence.ProtocolBufferPersistence;

import junit.framework.TestCase;

public class StoreBasedMessageBusTest extends TestCase {
  
  private static final String KEY = "foo";
  private static final String VALUE = "bar";
  
  private Persistence<byte[]> binaryStore = new MapBasedPersistence<byte[]>();
  private Persistence<MessageBusState> protoStore = new ProtocolBufferPersistence<MessageBusState>(
      binaryStore, MessageBusState.getDefaultInstance());
  private StoreBasedMessageBus bus = new StoreBasedMessageBus(null, binaryStore) {
    @Override
    String getRandomKey() {
      return KEY;
    }
  };
  private MessageParser parser = EasyMock.createMock(MessageParser.class);
  
  public void testOpen() {
    
    // Make sure that calling open once will populate the store
    EasyMock.replay(parser);
    assertNull(protoStore.get(KEY));
    bus.open(parser);
    assertNotNull(protoStore.get(KEY));
    EasyMock.verify(parser);
    
    // Make sure that calling a second time will cause a problem
    try {
      bus.open(parser);
      fail("Expected ConcurrentModificationException");
    } catch (ConcurrentModificationException expected) {
      // fall through
    }    
  }
  
  public void testCannotSetPropertyBeforeOpen() {
    try {
      bus.setProperty("foo", "bar");
      fail("Expected ConcurrentModificationException");
    } catch (ConcurrentModificationException expected) {
      // fall through
    }    
  }

  public void testCannotSendMessageBeforeOpen() {
    try {
      bus.send(new CommError(23));
      fail("Expected ConcurrentModificationException");
    } catch (ConcurrentModificationException expected) {
      // fall through
    }    
  }
  
  public void testCloseBeforeOpen() {
    try {
      bus.close();
      fail("Expected ConcurrentModificationException");
    } catch (ConcurrentModificationException expected) {
      // fall through
    }    
  }

  public void testSaveBeforeOpen() {
    try {
      bus.save();
      fail("Expected ConcurrentModificationException");
    } catch (ConcurrentModificationException expected) {
      // fall through
    }    
  }
  
  public void testGetPropertyBeforeOpen() {
    try {
      bus.getProperty("foo", "bar");
      fail("Expected ConcurrentModificationException");
    } catch (ConcurrentModificationException expected) {
      // fall through
    }    
  }

  public void testClose() {
    
    // First, check that a "close" cleans up the datastore
    testOpen();
    assertNotNull(protoStore.get(KEY));
    bus.close();
    assertNull(protoStore.get(KEY));
    
    // Try to close a second time should fail
    try {
      bus.close();
      fail("Expected ConcurrentModificationException");
    } catch (ConcurrentModificationException expected) {
      // fall through
    }    
  }
  
  public void testGetNonExistentProperty() {
    testOpen();
    assertNull(bus.getProperty(KEY, null));
    assertEquals(bus.getProperty(KEY, VALUE), VALUE);
  }
  
  public void testGetUnsavedProperty() {
    testOpen();
    bus.setProperty(KEY, VALUE);
    assertEquals(VALUE, bus.getProperty(KEY, null));
    assertEquals(protoStore.get(KEY).getConnectionPropertiesCount(), 0);
  }

  public void testGetSavedProperty() {
    testOpen();
    bus.setProperty(KEY, VALUE);
    bus.save();
    assertEquals(VALUE, bus.getProperty(KEY, null));
    assertEquals(protoStore.get(KEY).getConnectionPropertiesCount(), 1);
    
    // Try to load directly from the store and re-populate the cache
    bus = new StoreBasedMessageBus(KEY, binaryStore);
    assertEquals(VALUE, bus.getProperty(KEY, null));
  }
  
  public void testSendMessage() {
    testOpen();
    bus.send(new CommError(23));
    assertEquals(protoStore.get(KEY).getMessageQueueCount(), 0);
    bus.save();
    assertEquals(protoStore.get(KEY).getMessageQueueCount(), 1);
    OutgoingMessage element = protoStore.get(KEY).getMessageQueue(0);
    assertEquals(new CommError(23).flatten().toString(), element.getPayload());
    assertEquals(-1, element.getAckToken());
  }
}
