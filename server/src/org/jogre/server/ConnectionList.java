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

public interface ConnectionList {

  /**
   * Create a new Connection object using a ServerConnectionThread and add
   * to the connections HashMap using the username as the key.
   *
   * @param gameId 	       Game id e.g. "chess"
   * @param username         Username of person.
   * @param connectionThread ServerConnectionThread.
   */
  public abstract void addConnection(String gameId,
      String username,
      ServerConnectionThread connectionThread);

  /**
   * Set the administrator thread.
   *
   * @param gameId 	       Game id e.g. "chess"
   * @param username         Username of person.
   * @param connectionThread ServerConnectionThread.
   */
  public abstract void setAdminConnection(
      ServerConnectionThread connectionThread);

  /**
   * Return a serverconnection thread for a specified user (which is a field
   * of the Connection object).
   *
   * @param username Username of user.
   * @return
   */
  public abstract ServerConnectionThread getServerConnectionThread(
      String gameId, String username);

  /**
   * Return a Connection object of specified user.
   *
   * @param username
   * @return
   */
  public abstract Connection getConnection(String gameId,
      String username);

  /**
   * Remove a connection.
   *
   * @param username
   */
  public abstract void removeConnection(String gameId,
      String username);

  /**
   * Remove the admin connection.
   */
  public abstract void removeAdminConnection();

  /**
   * Return admin connection.
   * 
   * @return
   */
  public abstract ServerConnectionThread getAdminConnection();

}