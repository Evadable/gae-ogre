package com.appenginefan.jogre.webapp.client;

import com.google.gwt.user.client.ui.HTML;

public class SetMessageCommand extends ClientGameCommand {
  
  private static final long serialVersionUID = 20091006L;
  private String messageHtml;
  
  SetMessageCommand(){}
  
  public SetMessageCommand(String messageHtml) {
    this.messageHtml = messageHtml;
  }

  @Override
  void execute(Jogre_appengine client) {
    client.messageContainer.clear();
    client.messageContainer.add(new HTML(messageHtml));
  }

}
