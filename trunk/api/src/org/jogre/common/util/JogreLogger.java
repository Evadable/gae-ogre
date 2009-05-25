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
package org.jogre.common.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Wrapper around java logging. Use DownwardsCompatibleLogger in clients,
 * if you prefer old-school logging for your game</p>
 *
 * @author  Jens Scheffler
 * @version Alpha 0.2.3
 */
public class JogreLogger implements IJogreLog {
  
  // dont log constant.
  protected static final String DONT_LOG = "-1";
  
  /**
   * Interface that knows how to write log output data
   */
  public static interface LogWriter {
    
    /**
     * Method which performs the debug. Override this for customized log output
     *
     * @param logPriority Priority of the log (should be between 1 and 3)
     * @param method   Used to show class.method () in log. If this is equal
     *                 to DONT_LOG then this isnt displayed.
     * @param message  Message to log.
     */
    void log (int logPriority, String method, String message);
  }
  
  /**
   * Factory that produces log writers
   */
  public static interface LogWriterFactory {
    LogWriter getLogWriter(Class<?> loggedClass);
  }
  
  /**
   * Default implementation, uses Java logging
   */
  private static class DefaultLogWriter implements LogWriter {
    
    private static final Level[] LEVELS = {null, Level.SEVERE, Level.INFO, Level.FINE};
    
    private Logger logger;
    
    public DefaultLogWriter(Class<?> loggedClass) {
      logger = Logger.getLogger(loggedClass.getName());
    }

    @Override
    public void log(int logPriority, String method, String message) {
      if (logPriority == NONE) {
        return;
      }
      if (DONT_LOG.equals(method)) {
        return;
      }
      if (method != null) {
        message = "(" + method + "): " + message;
      }
      logPriority = Math.min(Math.max(0, logPriority), LEVELS.length - 1);
      logger.log(LEVELS[logPriority], message);
    }
  }
  
  static LogWriterFactory factory = new LogWriterFactory() {

    @Override
    public LogWriter getLogWriter(Class<?> loggedClass) {
      return new DefaultLogWriter(loggedClass);
    }
  };
  
  private final LogWriter writer;

	/**
	 * Constructor which takes the class of the logged Class.
	 * @param loggedClass
	 */
	public JogreLogger (Class<?> loggedClass) {
	  writer = factory.getLogWriter(loggedClass);
	}

	/**
	 * Log an error message.
	 *
	 * @param method   Method in a class.
	 * @param message  Message to log.
	 */
	public void error (String method, String message) {
		writer.log (ERROR, method, message);
	}

	/**
	 * Log an information message.
	 *
	 * @param method   Method in a class.
	 * @param message  Message to log.
	 *
	 */
	public void info (String method, String message) {
	  writer.log (INFO, method, message);
	}

	/**
	 * Log a debug message.
	 *
	 * @param method   Method in a class.
	 * @param message  Message to log.
	 */
	public void debug (String method, String message) {
	  writer.log (DEBUG, method, message);
	}

	/**
	 * Simple log which will go in at INFO level.
	 *
	 * @param message  Message to log
	 */
	public void log (String message) {
	  writer.log (INFO, DONT_LOG, message);
	}

	/**
	 * Logs a stacktrace.
	 *
	 * @param e  Exception object.
	 */
	public void stacktrace (Throwable e) {
	    error ("Exception", e.getMessage());
	}
}
