package common;
/**
 * @author Escallier Pierre
 * @file ClientServerProtocol.java
 * @date 20 juin 08
 */

/**
 * 
 */
public interface ClientServerProtocol
{
	static enum eEtats
	{
		Connected,
		OutOfGame,
		InNewGame,
		InGame,
		InPausedGame
	};
	
	static enum eEvenements
	{
		// Connected
		sendPrivateMessage,
		askFriendList,
		addFriend,
		removeFriend,
		
		// OutOfGame
		sendOutGameChatMessage,
		askNewGamesList,
		askMyCurrentGamesList,
		tryReconnectingGame,
		createGame,
		joinNewGame,
		
		// InNewGame
		askNewGameDatas,
		changeNewGameConfig,
		changeNewGamePlayerConfig,
		exitNewGame,
		sendNewGameChatMessage,
		startNewGame,
		
		// InGame
		onGameCommand,
		
		// InPausedGame
		sendGameChatMessage
	};
	
}
