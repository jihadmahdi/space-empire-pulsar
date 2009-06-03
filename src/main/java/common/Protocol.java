/**
 * @author Escallier Pierre
 * @file Protocol.java
 * @date 26 mai 2009
 */
package common;

import java.util.Set;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;


/**
 * Define the game protocol (RPC services interfaces).
 */
public interface Protocol
{
	/**
	 * Methods that can be called by the client at any game stage.
	 */
	public static interface ServerCommon
	{
		/**
		 * Return player list.
		 * @return Set<Player> currently connected players.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		Set<Player> getPlayerList() throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Return the current game config.
		 * @return GameConfig current game config.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		GameConfig getGameConfig() throws RpcException, StateMachineNotExpectedEventException;
	}
	
	/**
	 * Methods that can be called by the client while the game is pending creation.
	 */
	public static interface ServerGameCreation extends ServerCommon
	{
		/**
		 * Update the player config and broadcast it to other players.
		 * @param playerCfg New player config.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void updatePlayerConfig(PlayerConfig playerCfg) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Send a message to the GameCreation Chat.
		 * @param msg Message.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		void sendMessage(String msg) throws RpcException, StateMachineNotExpectedEventException;
		
		/**
		 * Updage the game configuration.
		 * @param gameCfg new game config.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 * @throws ServerPrivilegeException If player is not authorised (admin).
		 */
		void updateGameConfig(GameConfig gameCfg) throws RpcException, StateMachineNotExpectedEventException, ServerPrivilegeException;
	}
	
	/**
	 * Methods that can be called by the client while the game is running.
	 */
	public static interface ServerRunningGame extends ServerCommon
	{

		/**
		 * Return the current game turn information for this player.
		 * @return GameTurnInfos current game turn information for this player.
		 * @throws RpcException On connection error.
		 * @throws StateMachineNotExpectedEventException If server is not in GameCreation state.
		 */
		PlayerGameBoard getGameBoard() throws RpcException, StateMachineNotExpectedEventException;
		
	}
	
	/**
	 * Methods that can be called by the client while the game is paused.
	 */
	public static interface ServerPausedGame extends ServerCommon
	{
		
	}
	
	/**
	 * Methods that can be called by the server.
	 */
	public static interface Client
	{

		/**
		 * Server notify the client to refresh the player list.
		 * @param playerList New player list.
		 * @throws RpcException On connection error.
		 */
		void refreshPlayerList(Set<Player> playerList) throws RpcException;

		/**
		 * Server notify the client to refresh the game config. Client is expected to display it in GameCreation panel.
		 * @param gameCfg New game config.
		 * @throws RpcException On connection error.
		 */
		void refreshGameConfig(GameConfig gameCfg) throws RpcException;
		
		/**
		 * Server broadcast game creation message from another player.
		 * @param fromPlayer Sender player. 
		 * @param msg Message.
		 * @throws RpcException On connection error.
		 */
		void receiveGameCreationMessage(Player fromPlayer, String msg) throws RpcException;
		
	}
}
