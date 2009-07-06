package org.jogre.server;

import javax.servlet.http.HttpServletRequest;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import org.jogre.common.IJogre;
import org.jogre.common.MessageBus;
import org.jogre.common.TransmissionException;
import org.jogre.common.comm.ITransmittable;

import com.appenginefan.toolkit.common.ServerEndpoint;
import com.appenginefan.toolkit.common.WebConnectionServer;
import com.appenginefan.toolkit.persistence.Persistence;

import static com.google.common.base.Functions.constant;

/**
 * A ConnectionList implementation that uses stateless 
 * features to persist and recreate connections on the fly
 */
public class WebConnectionList implements WebConnectionServer.Receiver, ConnectionList {
  
  private static class Bus implements MessageBus {
    
    private final ServerEndpoint endpoint;
    
    public Bus(ServerEndpoint endpoint) {
      this.endpoint = endpoint;
    }

    @Override
    public void close() {
      endpoint.close();
    }

    @Override
    public String getProperty(String key,
        String defaultValue) {
      return endpoint.getProperty(key, defaultValue);
    }

    @Override
    public void open(MessageParser parser) {
      endpoint.open();
    }

    @Override
    public void send(ITransmittable transObject) {
      endpoint.send(transObject.flatten().toString());
    }

    @Override
    public void setProperty(String key, String valueOrNull) {
      endpoint.setProperty(key, valueOrNull);
    }    
  }
  
  private static String makeKey(String gameId, String userName) {
    return gameId + "###" + userName;
  }
  
  private final WebConnectionServer connectionServer;
  private AbstractGameServer gameServer;
  private final Persistence<String> userGame2Handle;
  private final ThreadLocal<InMemoryConnectionList> requestCache;
  private final ThreadLocal<HttpServletRequest> currentRequest;
  
  public WebConnectionList(
      WebConnectionServer connectionServer, 
      Persistence<String> userGame2Handle) {
    this.connectionServer = connectionServer;
    this.userGame2Handle = userGame2Handle;
    this.requestCache = new ThreadLocal<InMemoryConnectionList>();
    this.currentRequest = new ThreadLocal<HttpServletRequest>();
  }
  
  private InMemoryConnectionList getCache() {
    if (requestCache.get() == null) {
      requestCache.set(new InMemoryConnectionList());
    }
    return requestCache.get();
  }
  
  @Override
  public void onEmptyPayload(WebConnectionServer arg0,
      ServerEndpoint arg1, HttpServletRequest arg2) {
    this.currentRequest.set(arg2);
  }

  @Override
  public void receive(
      final WebConnectionServer server,
      final ServerEndpoint endpoint, 
      String message,
      HttpServletRequest req) {
    
    this.currentRequest.set(req);
    
    // Can we parse the payload?
    final XMLElement payload = new XMLElement();
    try {
      payload.parseString(message);
    } catch (XMLParseException e) {
      return;
    }
    
    // Wrap the endpoint in a message bus, then dispatch
    final Bus asBus = new Bus(endpoint);
    try {
      new ServerConnectionThread(asBus, gameServer).parse(payload);
    } catch (TransmissionException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void addConnection(String gameId, String username,
      ServerConnectionThread connectionThread) {
    Bus bus = (Bus) connectionThread.getMessageBus();
    userGame2Handle.mutate(
        makeKey(gameId, username),
        constant(bus.endpoint.getHandle()));
    getCache().addConnection(gameId, username, connectionThread);
  }

  @Override
  public void removeAdminConnection() {
    removeConnection(IJogre.ADMINISTRATOR, IJogre.ADMINISTRATOR);    
  }

  @Override
  public void removeConnection(String gameId,
      String username) {
    getCache().removeConnection(gameId, username);
    String handle = userGame2Handle.get(makeKey(gameId, username));
    if (handle != null) {
      userGame2Handle.mutate(
          makeKey(gameId, username), constant((String) null));
      ServerEndpoint endpoint = 
          connectionServer.fromHandle(currentRequest.get(), handle);
      if (endpoint != null) {
        endpoint.close();
      }
    }
  }

  @Override
  public Connection getConnection(String gameId,
      String username) {
    Connection result = getCache().getConnection(gameId, username);
    if (result != null) {
      return result;
    }
    String handle = userGame2Handle.get(makeKey(gameId, username));
    if (handle == null) {
      return null;
    }
    ServerEndpoint endpoint = connectionServer.fromHandle(currentRequest.get(), handle);
    if (endpoint == null) {
      return null;
    }
    Bus bus = new Bus(endpoint);
    ServerConnectionThread thread = new ServerConnectionThread(bus, gameServer);
    getCache().addConnection(gameId, username, thread);
    return new Connection(thread);
  }

  @Override
  public ServerConnectionThread getAdminConnection() {
    Connection conn = getConnection(IJogre.ADMINISTRATOR, IJogre.ADMINISTRATOR); 
    if (conn != null) {
      return conn.getServerConnectionThread();
    }
    return null;
  }

  @Override
  public ServerConnectionThread getServerConnectionThread(
      String gameId, String username) {
    if (gameId != null) {
      if (gameId.equals(IJogre.ADMINISTRATOR))
        return getConnection (IJogre.ADMINISTRATOR, IJogre.ADMINISTRATOR).getServerConnectionThread();
      else 
        return getConnection (gameId, username).getServerConnectionThread();
    }
    return null;
  }

  @Override
  public void setAdminConnection(
      ServerConnectionThread connectionThread) {
    addConnection (IJogre.ADMINISTRATOR, IJogre.ADMINISTRATOR, connectionThread);
  }

  public void setGameServer(AbstractGameServer gameServer) {
    this.gameServer = gameServer;
  }
  
  public WebConnectionServer getServer() {
    return connectionServer;
  }

}
