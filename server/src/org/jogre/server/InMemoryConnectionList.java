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

import java.util.HashMap;

import org.jogre.common.IJogre;

/**
 * List of Connection objects.
 *
 * @author  Bob Marks
 * @version Beta 0.3
 */
public class InMemoryConnectionList implements ConnectionList {

	/** Hashmap of connections. */
	private HashMap connections;

	/**
	 * Constructor which sets up a Hash to store the various Connection
	 * objects in.
	 */
	public InMemoryConnectionList () {
		this.connections = new HashMap ();
	}

	/* (non-Javadoc)
   * @see org.jogre.server.ConnectionList#addConnection(java.lang.String, java.lang.String, org.jogre.server.ServerConnectionThread)
   */
	public void addConnection (String gameId, String username, ServerConnectionThread connectionThread) {
		Connection conn = new Connection (connectionThread);
		
		connections.put (getKey (gameId, username), conn);
	}
	
	/* (non-Javadoc)
   * @see org.jogre.server.ConnectionList#setAdminConnection(org.jogre.server.ServerConnectionThread)
   */
	public void setAdminConnection (ServerConnectionThread connectionThread) {
		addConnection (IJogre.ADMINISTRATOR, IJogre.ADMINISTRATOR, connectionThread);
	}
	
	/* (non-Javadoc)
   * @see org.jogre.server.ConnectionList#getServerConnectionThread(java.lang.String, java.lang.String)
   */
	public ServerConnectionThread getServerConnectionThread (String gameId, String username) {
		if (gameId != null) {
			if (gameId.equals(IJogre.ADMINISTRATOR))
				return getConnection (IJogre.ADMINISTRATOR, IJogre.ADMINISTRATOR).getServerConnectionThread();
			else 
				return getConnection (gameId, username).getServerConnectionThread();
		}
		
		return null;
	}
	
	/* (non-Javadoc)
   * @see org.jogre.server.ConnectionList#getConnection(java.lang.String, java.lang.String)
   */
	public Connection getConnection (String gameId, String username) {
		return (Connection)connections.get (getKey (gameId, username));
	}

	/* (non-Javadoc)
   * @see org.jogre.server.ConnectionList#removeConnection(java.lang.String, java.lang.String)
   */
	public void removeConnection (String gameId, String username) {
		connections.remove (getKey (gameId, username));
	}
	
	/* (non-Javadoc)
   * @see org.jogre.server.ConnectionList#removeAdminConnection()
   */
	public void removeAdminConnection () {
		connections.remove (getKey (IJogre.ADMINISTRATOR, IJogre.ADMINISTRATOR));
	}

	/**
	 * Return hash key from a gameId and a username.
	 *
	 * @param gameId   GameID
	 * @param username Username
	 * @return
	 */
	private String getKey (String gameId, String username) {
	    return gameId + "-" + username;
	}

	/* (non-Javadoc)
   * @see org.jogre.server.ConnectionList#getAdminConnection()
   */
	public ServerConnectionThread getAdminConnection() {
		Connection conn = getConnection(IJogre.ADMINISTRATOR, IJogre.ADMINISTRATOR); 
		
		if (conn != null)
			return conn.getServerConnectionThread();
		 
		return null;
	}
}
