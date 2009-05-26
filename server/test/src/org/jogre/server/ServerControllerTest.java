package org.jogre.server;

import junit.framework.TestCase;

/**
 * Tests for the ServerController class
 * 
 * @author Jens Scheffler
 *
 */
public class ServerControllerTest extends TestCase {
  
  /**
   * A simple unit test that makes sure that the ServerController object can be
   * constructed. In the previous revision of the code, this would have thrown
   * a NullPointerException
   */
  public void testConstructor() {
    new ServerController("foo"){
      @Override
      public void gameOver(ServerConnectionThread conn,
          int tableNum, int resultType) {
      }
      @Override
      public void startGame(int tableNum) {
      }};
  }

}
