/*
* JOGRE (Java Online Gaming Real-time Engine) - Tetris
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
package org.jogre.tetris.client;

import org.jogre.client.TableConnectionThread;
import org.jogre.client.awt.JogreClientFrame;
import org.jogre.client.awt.JogreGlassPane;
import org.jogre.client.awt.JogreTableFrame;

/**
 * Tetris client frame.
 *
 * @author Bob Marks
 * @version Alpha 0.2.3
 */
public class TetrisClientFrame extends JogreClientFrame {
	
    /**
	 * Default constructor.
	 */
	public TetrisClientFrame (String [] args) {
		super (args);
	}

	/* (non-Javadoc)
     * @see org.jogre.client.awt.JogreClientFrame#getJogreTableFrame(org.jogre.client.TableConnectionThread)
     */
    public JogreTableFrame getJogreTableFrame (TableConnectionThread conn) {
        return new TetrisTableFrame (conn);
    }

	/**
	 * Main method which executes the class.
	 *
	 * @param args
	 */
	public static void main (String [] args) {
	    TetrisClientFrame client = new TetrisClientFrame (args);
	}
}