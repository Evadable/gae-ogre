package org.jogre.client.http;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Queue;

import nanoxml.XMLElement;

import org.easymock.EasyMock;
import org.jogre.common.TransmissionException;
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
  
  // mock object, auto-created by EasyMock
  private final HttpClientMessageBus.Environment environment = 
      EasyMock.createStrictMock(HttpClientMessageBus.Environment.class);
  private final MessageParser parser = 
      EasyMock.createStrictMock(MessageParser.class);
  
  
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
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE - 1));
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
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE - 1));
    environment.sleep(1L);
    
    // Second communication attempt succeeds -- the meta-value is "a"
    // We then sleep for a while again
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch("<payload></payload>")).andReturn("<payload meta=\"a\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE - 2));
    environment.sleep(2L);
    
    // Nothing is in the queue, so the next communication attempt will still be empty.
    // It will contain a meta-tag though
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch("<payload meta=\"a\"></payload>")).andReturn("<payload meta=\"b\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE - 3));
    environment.sleep(3L);
    
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
    
    // Push a few (MAX + 1) messages onto the bus
    for (int i = 0; i <= MAX; i++) {
      messageBus.send(new CommError(i + 1));
      
    }
    
    // First, a connection is established 
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch("<payload></payload>")).andReturn("<payload meta=\"a\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE + 1));
    
    // Next, the first three messages get delivered
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch(
        "<payload meta=\"a\">" +
        "<error status=\"1\"/>" +
        "<error status=\"2\"/>" +
        "<error status=\"3\"/></payload>")).andReturn("<payload meta=\"b\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE + 1));
    
    // Then, the fourth message gets delivered
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch(
        "<payload meta=\"b\">" +
        "<error status=\"4\"/></payload>")).andReturn("<payload meta=\"c\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE + 1));
    
    // Now, the queue should be empty. 
    // An empty payload gets delivered, just to poll for new messages
    // from the server.
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch(
        "<payload meta=\"c\"></payload>")).andReturn("<payload meta=\"d\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE - 1));
    environment.sleep(1L);

    // Let's try it out
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
  
  public void testResendIfCommunicationFailed() throws Exception {
    
    // Push a message onto the bus
    messageBus.send(new CommError(13));
    
    // First, a connection is established 
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch("<payload></payload>")).andReturn("<payload meta=\"a\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE + 1));
    
    // Next, the message gets delivered, but something goes wrong
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch(
        "<payload meta=\"a\"><error status=\"13\"/></payload>")).andReturn(null);
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE + 1));
    
    // Since communication failed, the thread should try to re-deliver
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch(
        "<payload meta=\"a\"><error status=\"13\"/></payload>")).andReturn("<payload meta=\"b\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE + 1));
    
    // Now, the queue should be empty. 
    // An empty payload gets delivered, just to poll for new messages
    // from the server.
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch(
        "<payload meta=\"b\"></payload>")).andReturn("<payload meta=\"c\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE - 1));
    environment.sleep(1L);

    // Let's try it out
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
  
  public void testSendResponseToParser() throws Exception {
    
    // Store incoming elements in a local list, then end the loop
    final List<XMLElement> incoming = new ArrayList<XMLElement>();
    final MessageParser bufferingParser = new MessageParser(){
      @Override
      public void cleanup() {
        fail("Cleanup should not be called");
      }
      @Override
      public void parse(XMLElement message)
          throws TransmissionException {
        incoming.add(message);
        messageBus.close();
      }};
      
    // Establish communication
    environment.execute(messageBus);
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch("<payload></payload>")).andReturn("<payload meta=\"a\"/>");
    EasyMock.expect(environment.currentTimeMillis()).andReturn((long) (SILENCE - 1));
    environment.sleep(1L);
    
    // After sleeping for a while, another request should go out.
    // Let's respond with something.
    EasyMock.expect(environment.currentTimeMillis()).andReturn(0L);
    EasyMock.expect(environment.fetch("<payload meta=\"a\"></payload>"))
      .andReturn("<payload meta=\"b\"><foo/><bar/></payload>");
    
    // That's it for now, let's try it out
    EasyMock.replay(environment);
    try {
      messageBus.open(bufferingParser);
      messageBus.run();
      fail("Expected an InterruptedException");
    } catch (InterruptedException expected) {
      // fall through
    }
    EasyMock.verify(environment);
    
    // Let's look at the incoming queue
    assertEquals(1, incoming.size());
    assertEquals("foo", incoming.get(0).getName());

  }
}
