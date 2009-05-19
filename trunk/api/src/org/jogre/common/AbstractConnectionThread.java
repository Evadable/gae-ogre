/*
 * JOGRE (Java Online Gaming Real-time Engine) - API
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
package org.jogre.common;

import java.io.IOException;
import java.net.Socket;

import nanoxml.XMLElement;

import org.jogre.common.util.JogreLogger;

/**
 * Represents a connection which is spawned with each client.  This
 * stores a way to communicate (read/write) Strings to the user/server.
 * Also the username of the client is stored in the username String.
 *
 * @author  Bob Marks
 * @version Alpha 0.2.3
 */
public abstract class AbstractConnectionThread {

	/** Logging */
	private JogreLogger logger = new JogreLogger (this.getClass());

	/** Communication between the server and the user. */
	private SocketBasedMessageBus messageBus;

	/** Username of the client. */
	protected String username;

	/**
	 * This abstract method must be overwritten by a child which extends this
	 * class.
	 *
	 * @param message       Communication as an XML object.
	 * @throws TransmissionException  This is thrown if there is a problem parsing the String.
	 */
	public abstract void parse (XMLElement message) throws TransmissionException;

	/**
	 * This method is called to properly clean up after a client.
	 */
	public abstract void cleanup ();

	/**
	 * Constructor for a connection which takes a Socket and sends up the input
	 * and output stream.
	 *
	 * @param socket   Socket connection to client / server.
	 */
	public AbstractConnectionThread (Socket socket) {
		try {
			logger.debug ("AbstractConnectionThread", "Creating new in/out streams.");
			this.messageBus = new SocketBasedMessageBus(this, socket);
		}
		catch (IOException ioEx) {
			logger.error ("AbstractConnectionThread", "IO Exception.");
			logger.stacktrace (ioEx);
		}
	}
	
	/**
	 * TODO: extract interface
	 * @return the messaging bus that this thread uses
	 */
	public SocketBasedMessageBus getMessageBus() {
	  return messageBus;
	}

	/**
	 * Returns the username.
	 *
	 * @return  Username of client / server who created this thread.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the username.
	 *
	 * @param username  Username of client / server who created this thread.
	 */
	public void setUsername (String username) {
		this.username = username;
	}
}