/**
 * @author Escallier Pierre
 * @file IServerUser.java
 * @date 3 juil. 2008
 */
package common;

/**
 * Describe Server side user interface, that can be called from client.
 * This interface should be implemented as a listener in server side, and as a sender in client side.
 */
public interface IServerUser
{
	/**
	 * User send private message to another user (it does not matter if they are in-game or not).
	 * @param user 
	 * @param msg
	 * @see IClientUser#receivePrivateMessage(User, String)
	 * @category Connected
	 * @category Chat
	 * @category Synchronized
	 */
	void sendPrivateMessage(String receiverName, String msg);
	
	/**
	 * Ask server for the user friend list.
	 * @see IClientUser#receiveFriendList(FriendList)
	 * @category Connected
	 * @category FriendList
	 * @category Synchronizing call
	 */
	void askFriendList();
	
	/**
	 * Add a new friend to the friend list.
	 * @param newFriend
	 * @category Connected
	 * @category FriendList
	 */
	void addFriend(String newFriendName);
	
	/**
	 * Remove a friend from the friend list.
	 * @param oldFriend
	 * @category Connected
	 * @category FriendList
	 */
	void removeFriend(String oldFriendName);

	/**
	 * Send a message to the general out of game channel.
	 * @param msg
	 * @see IClientUser#receiveOutGameChatMessage(User, String)
	 * @category Connected
	 * @category Chat
	 * @category Synchronized
	 */
	void sendOutGameChatMessage(String msg);
	
	/**
	 * Ask server for the list of new games.
	 * @see IClientUser#receiveNewGamesList(Vector)
	 * @category Connected
	 * @category GameBoard
	 * @category Synchronized
	 */
	void askNewGamesList();
	
	/**
	 * Ask server for the user current running games list.
	 * @see IClientUser#receiveMyCurrentGamesList(Vector)
	 * @category Connected
	 * @category GameBoard
	 * @category Synchronized
	 */
	void askMyCurrentGamesList();
	
	/**
	 * Ask the server to try to reconnect the user to the current running game.
	 * @param game
	 * @see IClientUser#reconnectingGame(Game)
	 * @category Connected
	 * @category GameBoard
	 * @category Synchronized
	 */
	void tryReconnectingGame(String gameName);
	
	/**
	 * Ask the server to create a new game.
	 * @param gameName
	 * @param gamePassword
	 * @param gameMaxPlayers
	 * @category Connected
	 * @category GameBoard
	 * @category Synchronized
	 */
	void createGame(String gameName, String gamePassword, int gameMaxPlayers);
	
	/**
	 * Ask the server to join user to the new game.
	 * @param newGame
	 * @see IClientUser#joinedNewGame(NewGame)
	 * @category Connected
	 * @category NewGame
	 */
	void joinNewGame(String newGameName);
	
	/**
	 * Send a message to the new game channel.
	 * @param msg
	 * @see IClientUser#receiveNewGameChatMessage(User, String)
	 * @category Connected
	 * @category NewGame
	 * @category Chat
	 */
	void sendNewGameChatMessage(String msg);
	
	/**
	 * Ask the server to change the current new game config.
	 * This can be done only by the current new game creator.
	 * @param gameConfig
	 * @see IClientUser#updateNewGameConfig(GameConfig)
	 * @category Connected
	 * @category NewGame
	 */
	void changeNewGameConfig(IGameConfig gameConfig);
	
	/**
	 * Ask the server to change the player config for the current new game.
	 * @param playerConfig
	 * @see IClientUser#updateNewGamePlayerConfig(PlayerConfig)
	 * @category Connected
	 * @category NewGame
	 */
	void changeNewGamePlayerConfig(IPlayerConfig playerConfig);
	
	/**
	 * Ask the server to send the NewGame datas.
	 * @see IClientUser#receiveNewGameDatas(GameConfig, Vector)
	 * @category Connected
	 * @category NewGame
	 * @category Synchronizing point
	 */
	void askNewGameDatas();
	
	/**
	 * Left the current joined new game room.
	 * @category Connected
	 * @category NewGame
	 */
	void exitNewGame();
	
	/**
	 * Ask the server to run the current new game.
	 * This can be done only by the current new game creator.
	 * @see IClientUser#startingNewGame(Game)
	 * @category Connected
	 * @category NewGame
	 */
	void startNewGame();
	
	/**
	 * Ask the server to proceed a game command.
	 * @param command
	 * @see IClientUser#onGameCommand(IGameCommand)
	 * @category Connected
	 * @category InGame
	 */
	void onGameCommand(IGameCommand command);
	
	/**
	 * Send a message to the current running game channel.
	 * @param msg
	 * @category Connected
	 * @category InGame
	 */
	void sendGameChatMessage(String msg);
}
