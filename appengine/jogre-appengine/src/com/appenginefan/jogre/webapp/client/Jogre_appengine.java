package com.appenginefan.jogre.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

/*
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Jogre_appengine implements EntryPoint, AsyncCallback<ClientGameCommand> {
  
  RootPanel messageContainer;
  String sessionName;
  
  /**
   * Create a remote service proxy to talk to the server-side Greeting service.
   */
  private final GameServiceAsync greetingService =
      GWT.create(GameService.class);

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    messageContainer = RootPanel.get("messageContainer");
    messageContainer.add(new HTML("Connecting to server..."));
    greetingService.greetServer(null, this);
  }

  @Override
  public void onFailure(Throwable caught) {
    // Some RPC went wrong; let's reconnect
    greetingService.greetServer(sessionName, this);
  }

  @Override
  public void onSuccess(ClientGameCommand result) {
    result.execute(this);
  }
}
