package org.jogre.server.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nanoxml.XMLElement;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.easymock.LogicalOperator;
import org.jogre.common.MessageBus;
import org.jogre.common.PayloadBuilder;
import org.jogre.common.MessageBus.MessageParser;

import junit.framework.TestCase;

/**
 * Unit tests for the CommunicationHandler
 * 
 * @author Jens Scheffler
 *
 */
public class CommunicationHandlerTest extends TestCase implements Comparator {
  
  private IMocksControl control;
  private MessageBusFactory factory;
  private CommunicationHandler handler;
  private HttpServletRequest req; 
  private HttpServletResponse resp;
  private ServletInputStream stream;
  private String requestBody;
  private MessageBus bus;
  private MessageParser parser;
  private int amountOfCallsToNewParser;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    control = EasyMock.createStrictControl();
    factory = control.createMock(MessageBusFactory.class);
    req = control.createMock(HttpServletRequest.class);
    resp = control.createMock(HttpServletResponse.class);
    bus = control.createMock(MessageBus.class);
    parser = control.createMock(MessageParser.class);
    stream = new ServletInputStream(){      
      private InputStream wrapped;

      @Override
      public int read() throws IOException {
        if (wrapped == null) {
          wrapped = new ByteArrayInputStream(requestBody.getBytes("UTF-8"));
        }
        return wrapped.read();
      }};
    EasyMock.expect(req.getInputStream()).andReturn(stream);
    handler = new CommunicationHandler(null, factory) {
      @Override
      MessageParser newParser(MessageBus bus) {
        amountOfCallsToNewParser++;
        return parser;
      }
    };
  }
  
  public void testUnparseableXml() throws IOException {
    requestBody = "foo";
    resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
    control.replay();
    handler.dispatch(req, resp);
    control.verify();
    assertEquals(0, amountOfCallsToNewParser);
  }

  public void testValidXmlButWrongTag() throws IOException {
    requestBody = "<foo/>";
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    control.replay();
    handler.dispatch(req, resp);
    control.verify();
    assertEquals(0, amountOfCallsToNewParser);
  }
  
  public void testNewConnection() throws IOException {
    requestBody = PayloadBuilder.payload(null).toString();
    EasyMock.expect(factory.loadMessageBus(req, "")).andReturn(null);
    EasyMock.expect(factory.newMessageBus(req)).andReturn(bus);
    bus.open(parser);
    factory.writeState(bus, resp);
    factory.commit(req);
    control.replay();
    handler.dispatch(req, resp);
    control.verify();
    assertEquals(1, amountOfCallsToNewParser);
  }

  public void testExistingConnection() throws IOException {
    requestBody = PayloadBuilder.payload("foo").toString();
    EasyMock.expect(factory.loadMessageBus(req, "foo")).andReturn(bus);
    factory.writeState(bus, resp);
    factory.commit(req);
    control.replay();
    handler.dispatch(req, resp);
    control.verify();
    assertEquals(1, amountOfCallsToNewParser);
  }
  
  public void testParseContent() throws Exception {
    requestBody = PayloadBuilder.payload("foo").addChild("<a/>").addChild("<b/>").toString();
    EasyMock.expect(factory.loadMessageBus(req, "foo")).andReturn(bus);
    parser.parse((XMLElement) EasyMock.cmp("<a/>", this, LogicalOperator.EQUAL));
    parser.parse((XMLElement) EasyMock.cmp("<b/>", this, LogicalOperator.EQUAL));
    factory.writeState(bus, resp);
    factory.commit(req);
    control.replay();
    handler.dispatch(req, resp);
    control.verify();
    assertEquals(1, amountOfCallsToNewParser);
  }
  
  public void testRollback() throws Exception {
    requestBody = PayloadBuilder.payload("foo").toString();
    EasyMock.expect(factory.loadMessageBus(req, "foo")).andReturn(bus);
    factory.writeState(bus, resp);
    RuntimeException ex = new RuntimeException("expected");
    EasyMock.expectLastCall().andThrow(ex);
    factory.rollback(req);
    control.replay();
    try {
      handler.dispatch(req, resp);
      fail("Should have thrown a RuntimeException");
    } catch (RuntimeException e) {
      assertEquals(e, ex);
    }
    control.verify();
    assertEquals(1, amountOfCallsToNewParser);
  }

  /**
   * Hack: compares objects of any type by comparing their string implementation
   */
  @Override
  public int compare(Object arg0, Object arg1) {
    String s1 = String.valueOf(arg0);
    String s2 = String.valueOf(arg1);
    return s1.compareTo(s2);
  }
}
