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

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Locale;

import org.jogre.common.IError;
import org.jogre.common.IJogre;
import org.jogre.common.JogreGlobals;
import org.jogre.common.SocketBasedMessageBus;
import org.jogre.common.util.DownwardsCompatibleLogger;
import org.jogre.common.util.JogreLabels;
import org.jogre.server.data.IServerData;
import org.jogre.server.data.ServerDataException;
import org.jogre.server.data.ServerDataFactory;
import org.jogre.server.data.db.DBConnection;

import com.appenginefan.toolkit.common.WebConnectionServer;
import com.appenginefan.toolkit.persistence.MapBasedPersistence;

/**
 * <p>This is the all important JogreServer class.</p>
 * 
 * <p>When a client connects to a <code>JogreServer</code> a new
 * <code>ServerConnectionThread</code> is created in its own Thread which will
 * handle all communciation between the server and the client. The connect
 * method returns the correct ServerConnectionThread to the server.</p>
 *
 * <p>If a server keeps state then the abstract <code>getJogreModel()</code> method should
 * return the correct implementation of the <code>JogreModel</code>. If a game should keep
 * state (this allows demanding games to run faster but means other users can't
 * join a table and watch a game) then this method should simply return null.</p>
 *
 * @author  Bob Marks
 * @version Beta 0.3
 */
public class JogreServer extends AbstractGameServer {

  /** Default server port. */
  public static final int DEFAULT_SERVER_PORT = 1790;

  /** Server port we are listening on. */
  protected int serverPort;

  private long startTime;

  /**
   * Default server constructor.
   */
  private JogreServer (ConnectionList connections, IServerData dataConnection) {
    super(connections, dataConnection);
    
    // Take note of start time
    startTime = System.currentTimeMillis();
    
    // Set default port
    serverPort = DEFAULT_SERVER_PORT;
    
    // Set default values such as ports
    setServerPort (ServerProperties.getInstance().getServerPort());
  }
  
  /**
   * Initialise the JOGRE server.
   * 
   * @throws ServerDataException 
   */
  public void init () {
    
    // Inject this instance into the connection list?
    if (getConnections() instanceof WebConnectionList) {
      ((WebConnectionList) getConnections()).setGameServer(this);
    }
    
    // Reset the server snapshot
    try {
      // Reset the database snapshot table
      getServerData().resetSnapshot (getGameList().getGameKeys ());
    } catch (ServerDataException e) {
      // If we get here it shows the database connection is not work so exit program
      e.printStackTrace();
      System.exit(-1);
    }
  }
  
  /**
   * <p>This method parses the commands handed in from the command prompt.
   * For example:</p>
   *
   * <code>-port=1234</code>
   *
   * <p>This sets the port for the server to listen on to 1234. More complex
   * servers should over write this method and handle its specific commands
   * here.</p>
   *
   * @param args     Additional arguments from the command line.
   */
  public void parseCommandLineArguments (String [] args) {
      if (args != null) {
      for (int i = 0; i < args.length; i++) {
        String argument = args [i];

        // Read the port if specified
        if (argument.startsWith("-port=")) {
          int pos = argument.indexOf ("=");
          try {
            int portNum = Integer.parseInt (argument.substring(pos + 1));
            setServerPort (portNum);
          } catch (NumberFormatException nfEx) {
            usage ();
          }
        }
                // Read the language
                else if (argument.startsWith("-lang=")) {
                    int pos = argument.indexOf ("=");
                    try {
                        String lang = argument.substring(pos + 1);
                        JogreGlobals.setLocale(lang);
                    } catch (Exception nfEx) {
                        usage ();
                    }
                }                
      }
      }
      
      // Print port and language
      Locale l = JogreGlobals.getLocale();
      System.out.println (labels.get("server.port") + ":\t\t" + getServerPort());
      System.out.println (labels.get("language") + ":\t\t" + l.getLanguage() + " (" +
        l.getDisplayLanguage() + ")");
  }

  /**
   * Prints out the usage of the server.
   */
  private void usage () {
        labels = ServerLabels.getInstance(); 
    System.out.println (labels.get ("jogre.server.version") + ": " + IJogre.VERSION);
    System.out.println ("\n" + labels.get ("usage") + ":");
    System.out.println ("\n\tjava org.jogre.server.JogreServer [" + labels.get("additional.arguments") + "]");
    System.out.println ("\n" + labels.get ("arguments") + ":");
    System.out.println ("\t-port=x         x=" + labels.get ("port.number.default.1790"));
        System.out.println ("\t-lang=x         x=" + JogreGlobals.SUPPORTED_LANGS);
  }
  
  /**
   * Run the server using the http protocol
   */
  private void runHttpBased() throws Exception {
    throw new UnsupportedOperationException("need to include jetty and comment this method in to make it work!!!");
//    System.out.println (labels.get("jogre.games.server.listening.on.port") + ": " + serverPort);
//    final WebConnectionList connections = (WebConnectionList) getConnections();
//    final org.eclipse.jetty.server.Handler handler =new org.eclipse.jetty.server.handler.AbstractHandler()  {
//        @Override
//        public void handle(String target, org.eclipse.jetty.server.Request req,
//            javax.servlet.http.HttpServletRequest request,
//            javax.servlet.http.HttpServletResponse response) throws IOException,
//            javax.servlet.ServletException {
//          connections.getServer().dispatch(connections, request, response);
//          ((org.eclipse.jetty.server.Request)request).setHandled(true);
//        }
//    };
//    org.eclipse.jetty.server.Server srv = new org.eclipse.jetty.server.Server(serverPort);
//    srv.setHandler(handler);
//    srv.start();
  }

  /**
   * Run method which runs a new server and listens for clients on the
   * specified port.
   */
  private void runSocketBased () {
    
    // Output welcome message to the console         
    System.out.println ("------------------------------------------------------------------");
    System.out.println (labels.get ("games.being.served") + " (" + getGameList().size() + "):");

    // Load the various games
    System.out.print (getGameLoader());

    // End of welcome message
    System.out.println ("------------------------------------------------------------------");

    // Declare server socket to listen for client connections
    ServerSocket listenSocket = null;

    try {
      // Set up the server first of all
      listenSocket = new ServerSocket (serverPort);

      System.out.println (labels.get("jogre.games.server.listening.on.port") + ": " + serverPort);
      
      // Show time server has started
      NumberFormat nf = NumberFormat.getInstance();
      nf.setGroupingUsed(true);
      long timeStarted = System.currentTimeMillis() - startTime;
      System.out.println ("\n" + labels.get("started.in", new String [] {nf.format(timeStarted)}));
      
      while (true) {
        // listen for and accept the connection
        Socket clientSocket = listenSocket.accept ();

        // Try to connect client to this server
        ServerConnectionThread conn = new ServerConnectionThread (
            new SocketBasedMessageBus(clientSocket), this);

        conn.getMessageBus().open(conn);
      }
    }
    catch (BindException bindEx) {
      System.out.println (labels.get("jogre.server.already.running.on.port") + ": " + serverPort);
      System.exit (0);
    }
    catch (Exception genEx) {
        genEx.printStackTrace();
    }

    // Close the server down again
    try {
      System.out.println  (labels.get("jogre.games.server.closing") + ": " + serverPort);

      if (listenSocket != null)
          listenSocket.close();
    }
    catch (IOException ioEx) {}
  }

  /**
   * Set the server port.
   *
   * @param serverPort
   */
  public void setServerPort (int serverPort) {
    this.serverPort = serverPort;
  }

  /**
   * Return the server port.
   *
   * @return
   */
  public int getServerPort () {
    return serverPort;
  }

  /**
   * Perform some basic tests before server starts up properly e.g. check the server
   * port 
   */
  public void initialTests () {
    ServerProperties serverProps = ServerProperties.getInstance();
    String dataType = serverProps.getCurrentServerData();
    System.out.println (labels.get("persistent.data") + ":\t" + dataType);
        if (dataType.equals (IServerData.DATABASE)) {
          String curDatabaseConn = serverProps.getCurrentDatabaseConnection();
          System.out.println (labels.get("database.connection") + ":\t" + curDatabaseConn);
          System.out.println (labels.get("database.url") + ":\t\t" + serverProps.getConnectionURL(curDatabaseConn));
          System.out.println (labels.get("database.driver") + ":\t" + serverProps.getConnectionDriver(curDatabaseConn));
        }
    
    // TEST 1: Ensure port is available
    try { 
      System.out.println ("------------------------------------------------------------------");
      System.out.println (labels.get("inital.tests") + ":");
      ServerSocket testSocket = new ServerSocket (serverPort);
      System.out.println ("\t" + labels.get("server.port.is.available"));
      testSocket.close();     
    }   
    catch (BindException bindEx) {
      System.out.println (labels.get("jogre.server.already.running.on.port") + ": " + serverPort);
      System.exit (-1);
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    // TEST 2: Test if using database that connection exists
    if (dataType.equals(IServerData.DATABASE)) {
      int error = IError.NO_ERROR;

      error = DBConnection.testConnection(serverProps.getDBDriver(),
                                      serverProps.getDBConnURL(),
                                      serverProps.getDBUsername(),
                                      serverProps.getDBPassword(),
                                      true);
      // Check error code
      if (error != IError.NO_ERROR) {
        System.out.println (JogreLabels.getError(error) + " " + serverProps.getDBConnURL());
        System.exit (-1);
      }
    }
  }

  /**
   * Main method which creates a single instance of the server,
   * parses the commandline arguments and then runs the server.
   *
   * @param args     Additional arguments from command line.
   */
  public static void main (String [] args) throws Exception {
    
    // Set up downwards compatible logging
    DownwardsCompatibleLogger.install();
    
    // Setup server properties (read "server.xml" file)
    ServerProperties.setUpFromFile();
    
    // Set up the connection list
    final String protocol = ServerProperties.getInstance().getProtocol();
    ConnectionList connections = new InMemoryConnectionList();
    if ("http".equalsIgnoreCase(protocol)) {
      final WebConnectionServer comm = 
        WebConnectionServer.fromPeristence(new MapBasedPersistence<byte[]>());
      connections = new WebConnectionList(comm, new MapBasedPersistence<String>());
    }
    
    // Initialise the server
    JogreServer server = new JogreServer(connections, ServerDataFactory.getInstance ());
    
    // Type initial games server info.
    System.out.println ("------------------------------------------------------------------");
    System.out.println ("                J O G R E   G A M E S   S E R V E R");
    System.out.println ("------------------------------------------------------------------");
    System.out.println (server.labels.get("author") + ":\t\t\tBob Marks");
    System.out.println (server.labels.get("version") + ":\t\t" + IJogre.VERSION);
    System.out.println (ServerLabels.getInstance().get("server.properties") + ":\t" + ServerProperties.getInstance().getServerFile().getAbsolutePath());

    // Parse the command line arguments
    server.parseCommandLineArguments (args);

    // Perform basic tests to ensure server can run
    server.initialTests ();
    
    // Setup server and run the server
    server.init();    
    if ("http".equalsIgnoreCase(protocol)) {
      server.runHttpBased();
    } else {
      server.runSocketBased ();
    }
  }

}
