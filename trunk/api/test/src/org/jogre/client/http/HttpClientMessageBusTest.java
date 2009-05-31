package org.jogre.client.http;

import java.util.ConcurrentModificationException;
import java.util.Queue;

import org.easymock.EasyMock;
import org.jogre.common.MessageBus.MessageParser;
import org.jogre.common.comm.CommError;
import org.jogre.common.comm.ITransmittable;

import junit.framework.TestCase;

/**
 * Unit tests for the HttpClientMessageBus
 * 
 * @author Jens Scheffler
 *
 */
public class HttpClientMessageBusTest extends TestCase {
  
  private static final int SILENCE = 5000;
  private static final int MAX = 3;
  
  // mock objects, auto-created by EasyMock
  private final HttpClientMessageBus.Environment environment = 
      EasyMock.createMock(HttpClientMessageBus.Environment.class);
  private final MessageParser parser = 
      EasyMock.createMock(MessageParser.class);
  
  
  private final HttpClientMessageBus messageBus = 
    new HttpClientMessageBus(environment, SILENCE, MAX);
  private final Queue<String> queue = messageBus.getQueue();
  
  @Override
  protected void setUp() throws Exception {
    assertNotNull(queue);
    assertEquals(0, queue.size());
  }
  
  /**
   * Tests that the get/set mechanism of properties works as expected.
   */
  public void testGetAndSetProperties() {
    assertNull(messageBus.getProperty("foo", null));
    assertEquals("bar",messageBus.getProperty("foo", "bar"));
    assertEquals("foobar",messageBus.getProperty("foo", "foobar"));
    
    messageBus.setProperty("foo", "bar");
    assertEquals("bar",messageBus.getProperty("foo", null));
    assertEquals("bar",messageBus.getProperty("foo", "bar"));
    assertEquals("bar",messageBus.getProperty("foo", "foobar"));
    
    messageBus.setProperty("foo", null);
    assertNull(messageBus.getProperty("foo", null));
    assertEquals("bar",messageBus.getProperty("foo", "bar"));
    assertEquals("foobar",messageBus.getProperty("foo", "foobar"));
  }
  
  /**
   * Tests that sending messages puts xml snippets into the local queue.
   */
  public void testSend() {
    ITransmittable message1 = new CommError(23);
    ITransmittable message2 = new CommError(24);
    messageBus.send(message1);
    assertEquals(1, queue.size());
    assertEquals(message1.flatten().toString(), queue.peek());
    
    messageBus.send(message2);
    assertEquals(2, queue.size());
    assertEquals(message1.flatten().toString(), queue.poll());
    assertEquals(message2.flatten().toString(), queue.poll());
  }
  
  public void testCannotOpenTwice() throws Exception {

    // Call "open" once. run() will not be executed,
    // since our environment is smart enough to intercept that
    environment.execute(messageBus);
    EasyMock.replay(environment, parser);
    messageBus.open(parser);
    EasyMock.verify(environment, parser);
    
    // Call "open" a second time, which should fail
    try {
      messageBus.open(parser);
      fail("second open() should have caused an exception");
    } catch (ConcurrentModificationException expected) {
      // fall through
    }    
  }
  
  public void testCloseWillTerminateRun() throws Exception {
    messageBus.close();
    EasyMock.replay(environment, parser);
    try {
      messageBus.run();
      fail("Expected an InterruptedException");
    } catch (InterruptedException expected) {
      // fall through
    }
    EasyMock.verify(environment, parser);
  }

  public void testSimpleLoop() throws Exception {
    
    // Record a very simple sequence for the environment:
    // The first http-send will fail, which will cause the
    // thread to sleep. We will jump out of the loop by
    // throwing an InterruptedException
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch("<payload></payload>")).andReturn(null);
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE + 1));
    environment.sleep(1L);
    EasyMock.expectLastCall().andThrow(new InterruptedException("end of test"));
    
    // Now, let's try this out
    EasyMock.replay(environment, parser);
    try {
      messageBus.run();
      fail("Expected an InterruptedException");
    } catch (InterruptedException expected) {
      // fall through
    }
    EasyMock.verify(environment, parser);
  }
  
  public void testEstablishConnectionInSecondAttempt() throws Exception {

    // First communication fails, so we sleep for a while
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch("<payload></payload>")).andReturn(null);
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE + 1));
    environment.sleep(1L);
    
    // Second communication attempt succeeds -- the meta-value is "a"
    // We then sleep for a while again
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch("<payload></payload>")).andReturn("<payload meta=\"a\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE + 1));
    environment.sleep(1L);
    
    // Nothing is in the queue, so the next communication attempt will still be empty.
    // It will contain a meta-tag though
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch("<payload meta=\"a\"></payload>")).andReturn("<payload meta=\"b\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE + 1));
    environment.sleep(1L);
    
    // That's it for now, let's interrup the thread and try it out
    EasyMock.expectLastCall().andThrow(new InterruptedException("end of test"));
    EasyMock.replay(environment, parser);
    try {
      messageBus.run();
      fail("Expected an InterruptedException");
    } catch (InterruptedException expected) {
      // fall through
    }
    EasyMock.verify(environment, parser);
  }
  
  public void testSendUntilQueueIsEmpty() throws Exception {
    // TODO: implement
  }
  
  public void testResendIfCommunicationFailed() throws Exception {
    // TODO: implement
  }
  
  public void testSendResponseToParser() throws Exception {
    // TODO: implement
  }
}
