package org.jogre.server.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import org.jogre.common.MessageBus;
import org.jogre.common.PayloadBuilder;
import org.jogre.common.TransmissionException;
import org.jogre.common.util.JogreLogger;
import org.jogre.server.AbstractGameServer;
import org.jogre.server.ServerConnectionThread;

/**
 * A simple servlet-like class that can receive incoming http communication.
 * Can be used in web-based systems by servlets or filters.
 * 
 * @author Jens Scheffler
 *
 */
public class CommunicationHandler {
  
  private final AbstractGameServer gameServer;
  private final MessageBusFactory busFactory;
  private final JogreLogger logger = new JogreLogger(CommunicationHandler.class);
  
  public CommunicationHandler(AbstractGameServer gameServer, MessageBusFactory busFactory) {
    this.gameServer = gameServer;
    this.busFactory = busFactory;
  }

  /**
   * Dispatches an incoming request
   * @param req
   * @param resp
   * @throws IOException
   */
  public void dispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // Can we parse the payload?
    final XMLElement payload = new XMLElement();
    final Reader reader = new InputStreamReader(req.getInputStream());
    try {
      payload.parseFromReader(reader);
    } catch (XMLParseException e) {
      resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
      return;
    }
    if (!payload.getName().equals(PayloadBuilder.TAG)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    
    // New or existing connection?
    MessageBus bus = busFactory.loadMessageBus(
        req, payload.getAttribute(PayloadBuilder.META, "").toString());
    boolean newBus = false;
    if (bus == null) {
      bus = busFactory.newMessageBus(req);
      newBus = true;
    }
    ServerConnectionThread conn = new ServerConnectionThread (bus, gameServer);
    if (newBus) {
      bus.open(conn);
    }
    
    // Process the payload
    for (XMLElement child : (Vector<XMLElement>)payload.getChildren()) {
      try {
        conn.parse(child);
      } catch (TransmissionException e) {
        logger.error("dispatch", "Error while processing " + child);
        logger.stacktrace(e);
      }
    }
    
    // Now, put some data into the response and return
    busFactory.writeState(bus, resp);
  }
  
}
