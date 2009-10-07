package com.appenginefan.jogre.webapp.client;

import java.io.Serializable;

public abstract class ClientGameCommand implements Serializable {
  
  abstract void execute(Jogre_appengine client);

}
