/**
 * @author Escallier Pierre
 * @file IClientUser.java
 * @date 3 juil. 2008
 */
package common;

import java.util.Vector;

/**
 * Describe Client side user interface, that can be called from server.
 * This interface should be implemented as a listener in client side, and as a sender in server side.
 */
public interface IClientUser
{
	/**
	 * Called when the user received a private message from another user.
	 * @param user
	 * @param msg
	 * @see IServerUser#sendPrivateMessage(User, String)
	 */
	void receivePrivateMessage(User sender, String msg);
	
	/**
	 * Receive friend list (with current states).
	 * @param friendList
	 * @see IServerUser#askFriendList()
	 */
	void receiveFriendList(FriendList friendList);
	
	/**
	 * Received when a friend get connected.
	 * @param onlineFriend
	 */
	void onFriendConnection(User onlineFriend);
	
	/**
	 * Received when a friend logs out.
	 * @param offlineFriend
	 */
	void onFriendDeconnection(User offlineFriend);
	
	/**
	 * Receive a message on the general out of game channel.
	 * @param sender
	 * @param msg
	 * @see IServerUser#sendOutGameChatMessage(String)
	 */
	void receiveOutGameChatMessage(User sender, String msg);
	
	/**
	 * Receive new games list from the server.
	 * @param newGamesList
	 * @see IServerUser#askNewGamesList()
	 */
	void receiveNewGamesList(Vector<NewGame> newGamesList);
	
	/**
	 * Receive the user current games list.
	 * @param currentGamesList
	 * @see IServerUser#askMyCurrentGamesList()
	 */
	void receiveMyCurrentGamesList(Vector<Game> currentGamesList);
	
	/**
	 * The user just joined a new game.
	 * @param newGame
	 * @see IServerUser#joinNewGame(NewGame)
	 */
	void joinedNewGame(NewGame newGame);
	
	/**
	 * Receive message on the new game channel the user connected to.
	 * @param sender
	 * @param msg
	 * @see IServerUser#sendNewGameChatMessage(String)
	 */
	void receiveNewGameChatMessage(User sender, String msg);
	
	/**
	 * Received when the current new game config has been changed (by the game creator).
	 * @param gameConfig
	 * @see IServerUser#changeNewGameConfig(GameConfig)
	 */
	void updateNewGameConfig(IGameConfig gameConfig);
	
	/**
	 * Received when a player config change for the current new game.
	 * @param playerConfig
	 * @see IServerUser#changeNewGamePlayerConfig(PlayerConfig)
	 */
	void updateNewGamePlayerConfig(IPlayerConfig playerConfig);
	
	/**
	 * Received the current new game complete data set.
	 * @param gameConfig
	 * @param playersConfigs
	 * @see IServerUser#askNewGameDatas()
	 */
	void receiveNewGameDatas(IGameConfig gameConfig, Vector<IPlayerConfig> playersConfigs);
	
	/**
	 * Received when the current new game is starting.
	 * @param game
	 * @see IServerUser#runNewGame();
	 */
	void startingNewGame(Game game);
	
	/**
	 * Received when reconnecting a running game.
	 * @param game
	 */
	void reconnectingGame(Game game);
	
	/**
	 * Received when the server need the client to proceed a game command.
	 * @param command
	 * @see IServerUser#onGameCommand(IGameCommand)
	 */
	void onGameCommand(IGameCommand command);
	
	/**
	 * Received when the current running game is paused by server (because at least one player is missing).
	 */
	void onGamePaused();
	
	/**
	 * Receive message on the current running game channel from the sender.
	 * @param sender
	 * @param msg
	 */
	void receiveGameChatMessage(User sender, String msg);
	
	/**
	 * Received when the current running game was paused by the server and is now resumed (because all the player are connected again, or time is out for them).
	 */
	void onGameResume(Game game);
}
