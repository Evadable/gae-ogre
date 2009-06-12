package org.jogre.server.http;

import java.util.ConcurrentModificationException;

import nanoxml.XMLElement;

import org.easymock.EasyMock;
import org.jogre.common.TransmissionException;
import org.jogre.common.MessageBus.MessageParser;
import org.jogre.common.comm.CommError;
import org.jogre.server.data.ProtoSchema.MessageBusState;

import com.appenginefan.toolkit.persistence.MapBasedPersistence;
import com.appenginefan.toolkit.persistence.Persistence;
import com.appenginefan.toolkit.persistence.ProtocolBufferPersistence;

import junit.framework.TestCase;

public class StoreBasedMessageBusTest extends TestCase {
  
  private static final String KEY = "foo";
  
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

  public void testGetPropertyBeforeOpen() {
    try {
      bus.getProperty("foo", "bar");
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
}
