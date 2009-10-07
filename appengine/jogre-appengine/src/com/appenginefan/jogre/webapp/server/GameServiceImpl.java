package com.appenginefan.jogre.webapp.server;

import com.appenginefan.jogre.webapp.client.ClientGameCommand;
import com.appenginefan.jogre.webapp.client.GameService;
import com.appenginefan.jogre.webapp.client.SetMessageCommand;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/*
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GameServiceImpl
    extends RemoteServiceServlet implements GameService {
  
  private static enum CssClass {
    ALERT, INFO
  }
  
  private User getCurrentUser() {
    return UserServiceFactory.getUserService().getCurrentUser();
  }
  
  private String escapeHtml(String text) {
    //todo: implement
    return text;
  }
  
  private String createSpan(CssClass cssClass, String escapedHtml) {
    return String.format("<span class='%s'>%s</span>", cssClass.name(), escapedHtml);
  }

  public ClientGameCommand greetServer(String input) {
    User user = getCurrentUser();
    if (user == null) {
      return new SetMessageCommand(createSpan(CssClass.ALERT, String.format(
          "<a href='%s'>Please click here to log in with your Google Account</a>",
          UserServiceFactory.getUserService().createLoginURL("/"))));
    }
    return new SetMessageCommand(createSpan(CssClass.ALERT, String.format(
        "Welcome, %s!",
        user.getNickname())));
  }
}
