package com.appenginefan.jogre.webapp.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/*
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("jogre")
public interface GameService extends RemoteService {
  ClientGameCommand greetServer(String name);
}
