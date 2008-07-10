/**
 * @author Escallier Pierre
 * @file IServerClientServerEventExecutor.java
 * @date 9 juil. 2008
 */
package server;

import common.Game;

/**
 * Describes SEPServerClientStateMachine externals events.
 * Should be implemented and called server side.
 */
public interface IServerClientServerEventExecutor
{
	/**
	 * Called when the current running game is paused by server (because at least one player is missing).
	 */
	void onGamePaused();
	
	/**
	 * Called when the current running game was paused by the server and is now resumed (because all the player are connected again, or time is out for them).
	 * @param game
	 */
	void onGameResume(Game game);
}
