/*
 * JOGRE (Java Online Gaming Real-time Engine) - Server
 * Copyright (C) 2004  Bob Marks (marksie531@yahoo.com)
 * http://jogre.sourceforge.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jogre.server;

import org.jogre.common.GameList;
import org.jogre.common.util.JogreLogger;
import org.jogre.server.controllers.ServerControllerList;
import org.jogre.server.data.IServerData;

public abstract class AbstractGameServer {

  /** Logging */
  JogreLogger logger = new JogreLogger (this.getClass());
  
  /** Boolean to specify if this server tries to link to a master server. */
  private boolean linkToMasterServer = false;

  /** List of server connenction objects. */
  private final ConnectionList connections;

  /** Link to the various games, and tables within games. */
  private GameList gameList;
  
  /** Link to the various parsers. */
  private ServerControllerList serverControllerList;

  /** Game loader. */
  private GameLoader gameLoader;
  
  /** Declare how a user connection. */
  private final IServerData dataConnection;
      
  ServerLabels labels = ServerLabels.getInstance();   // convience link to server labels
    
  /**
   * Default server constructor.
   */
  AbstractGameServer (ConnectionList connections, IServerData dataConnection) {
    
    // Set fields
    this.connections = connections;
    this.dataConnection = dataConnection;
    this.serverControllerList = new ServerControllerList (this);
    this.gameList             = new GameList ();
    this.gameLoader           = new GameLoader (gameList, serverControllerList, this);
  }

  /**
   * Return the current list of games on this server.
   *
   * @return
   */
  public GameList getGameList () {
    return gameList;
  }

  /**
   * Set the link to the master server.
   *
   * @param linkToMasterServer
   */
  public void setLinkToMasterServer (boolean linkToMasterServer) {
    this.linkToMasterServer = linkToMasterServer;
  }

  /**
   * Return true if this server wishes to try and link to the
   * master JOGRE game server.
   *
   * @return
   */
  public boolean isLinkToMasterServer () {
    return linkToMasterServer;
  }

  /**
   * Return the ConnectionList object of connections to the server.
   *
   * @return
   */
  public ConnectionList getConnections () {
    return connections;
  }

  /**
   * Return the server parser list which includes the standard base, game
   * and table parser.  Also includes parsers for each specific game.
   *
   * @return   List of server parsers
   */
  public ServerControllerList getControllers () {
    return serverControllerList;
  }

  /**
   * Return the connection to the users.
   * 
   * @return
   */
  public IServerData getServerData () {
    return dataConnection;
  }

  /**
   * Return the game loader.
   * 
   * @return
   */
  public GameLoader getGameLoader() {
    return gameLoader;
  }
  
}
