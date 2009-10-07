package com.appenginefan.jogre.webapp.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/*
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GameServiceAsync {
  void greetServer(String input,
      AsyncCallback<ClientGameCommand> callback);
}
