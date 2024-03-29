/*
 * JOGRE (Java Online Gaming Real-time Engine) - Chinese Checkers
 * Copyright (C) 2007  Richard Walter (rwalter42@yahoo.com)
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
package org.jogre.chineseCheckers.server;

import nanoxml.XMLElement;

import org.jogre.common.Table;
import org.jogre.common.IGameOver;
import org.jogre.common.JogreModel;
import org.jogre.common.comm.CommGameOver;
import org.jogre.common.util.JogreUtils;
import org.jogre.chineseCheckers.client.ChineseCheckersModel;
import org.jogre.chineseCheckers.common.CommChineseCheckersMakeMove;
import org.jogre.server.ServerConnectionThread;
import org.jogre.server.ServerController;

/**
 * Server controller for the game Chinese Checkers
 *
 * @author Richard Walter
 * @version Alpha 0.2.3
 */
public class ChineseCheckersServerController extends ServerController {

	/**
	 * Constructor to create a ChineseCheckers controller.
	 *
	 * @param gameKey  Game key.
	 */
	public ChineseCheckersServerController (String gameKey) {
		super (gameKey);
	}

	/**
	 * Create a new model when the game starts.
	 *
	 * @see org.jogre.server.ServerController#startGame(int)
	 */
	public void startGame (int tableNum) {
		Table theTable = getTable(tableNum);
		int numPlayers = theTable.getNumOfPlayers();
		boolean longJumps = "t".equals(theTable.getProperty("longJumps"));
		setModel (tableNum, new ChineseCheckersModel (numPlayers, longJumps));
	}

	/**
	 * Handle receiving objects from the clients
	 *
	 * By the time this is called, all of the clients have already been copied
	 * on the message.  So, this doesn't have to do that.
	 *
	 * @param   model        The model that the message is being made against
	 * @param   object       The object from the client
	 * @see org.jogre.client.JogreController#receiveObject(nanoxml.XMLElement)
	 */
	public void receiveObject (JogreModel model, XMLElement object) {
		if (object.getName().equals (CommChineseCheckersMakeMove.XML_NAME)) {
			// Make the move on the server model
			CommChineseCheckersMakeMove theMove = new CommChineseCheckersMakeMove (object);

			((ChineseCheckersModel)model).makeMove(theMove.getMoveVector());
		}
	}

	/**
	 * Verify that the game is over.
	 * (A client has just told us that the game is over, but we ought to verify it)
	 *
	 * @see org.jogre.server.ServerController#gameOver(org.jogre.server.ServerConnectionThread, int, int)
	 */
	public void gameOver (ServerConnectionThread conn, int tableNum, int resultType) {
		ChineseCheckersModel model = (ChineseCheckersModel) getModel(tableNum);
		int this_id = getSeatNum(conn.getUsername(), tableNum);

		if (model.checkWinner(this_id)) {
			gameOver (conn, tableNum, conn.getUsername(), IGameOver.WIN);
		}
	}

}
