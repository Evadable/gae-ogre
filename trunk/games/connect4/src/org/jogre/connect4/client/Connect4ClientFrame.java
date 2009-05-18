/*
 * JOGRE (Java Online Gaming Real-time Engine) - Connect4
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
package org.jogre.connect4.client;

import org.jogre.client.TableConnectionThread;
import org.jogre.client.awt.JogreClientFrame;
import org.jogre.client.awt.JogreTableFrame;

// Tic-tac-toe client frame.
public class Connect4ClientFrame extends JogreClientFrame {

  // Constructor
  public Connect4ClientFrame (String [] args) {
	super (args);
  }

  // Return the correct table frame
  public JogreTableFrame getJogreTableFrame (TableConnectionThread conn) {
    return new Connect4TableFrame (conn);
  }

  // Main method where this class gets run
  public static void main (String [] args) {
    Connect4ClientFrame frame = new Connect4ClientFrame (args);
  }   
}
