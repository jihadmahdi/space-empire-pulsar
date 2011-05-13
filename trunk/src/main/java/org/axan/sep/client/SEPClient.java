/**
 * @author Escallier Pierre
 * @file SEPClient.java
 * @date 3 avr. 2009
 */
package org.axan.sep.client;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.client.GameClient;
import org.axan.eplib.gameserver.common.IClientUser;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.eplib.gameserver.common.IServerUser.ServerSavingGameException;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.Player;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol;




/**
 * 
 */
public class SEPClient
{
	public static Logger log = Logger.getLogger(SEPClient.class.getCanonicalName());
	
	public static interface IUserInterface extends IClientUser
	{
		/* (non-Javadoc)
		 * @see org.axan.eplib.gameserver.common.IClientUser#displayGameCreationPanel()
		 */
		@Override
		void displayGameCreationPanel();

		/* (non-Javadoc)
		 * @see org.axan.eplib.gameserver.common.IClientUser#onGamePaused()
		 */
		@Override
		void onGamePaused();

		/* (non-Javadoc)
		 * @see org.axan.eplib.gameserver.common.IClientUser#onGameRan()
		 */
		@Override
		void onGameRan();

		/* (non-Javadoc)
		 * @see org.axan.eplib.gameserver.common.IClientUser#onGameResumed()
		 */
		@Override
		void onGameResumed();
		
		/**
		 * Update player list.
		 * Must return fast (let's spawn a new thread to make the job).
		 * @param playerList new player list.
		 */
		void refreshPlayerList(Set<Player> playerList);

		/**
		 * New GameCreation message received from another player.
		 * Must return fast.
		 * @param fromPlayer
		 * @param msg
		 */
		void receiveGameCreationMessage(Player fromPlayer, String msg);

		/**
		 * Refresh game config display.
		 * @param gameCfg New game config.
		 */
		void refreshGameConfig(GameConfig gameCfg);

		/**
		 * New RunningGame message received from another player.
		 * Must return fast.
		 * @param fromPlayer
		 * @param msg
		 */
		void receiveRunningGameMessage(Player fromPlayer, String msg);

		/**
		 * New turn has been sent from the server. Client must refresh the gameboard.
		 * @param gameBoard
		 */
		void receiveNewTurnGameBoard(PlayerGameBoard gameBoard);

		/**
		 * New PausedGame message received from another player.
		 * Must return fast.
		 * @param fromPlayer
		 * @param msg
		 */
		void receivePausedGameMessage(Player fromPlayer, String msg);
	}
	
	private static class SEPClientProtocol implements Protocol.Client
	{
		
		private SEPClient client;
		
		/**
		 * 
		 */
		public SEPClientProtocol(SEPClient client)
		{
			this.client = client;
		}

		/* (non-Javadoc)
		 * @see common.Protocol.Client#refreshPlayerList(java.util.Set)
		 */
		@Override
		public void refreshPlayerList(Set<Player> playerList)
		{
			client.ui.refreshPlayerList(playerList);
		}

		/* (non-Javadoc)
		 * @see common.Protocol.Client#receiveGameCreationMessage(common.Player, java.lang.String)
		 */
		@Override
		public void receiveGameCreationMessage(Player fromPlayer, String msg)
		{
			client.ui.receiveGameCreationMessage(fromPlayer, msg);
		}

		/* (non-Javadoc)
		 * @see common.Protocol.Client#refreshGameConfig(common.GameConfig)
		 */
		@Override
		public void refreshGameConfig(GameConfig gameCfg)
		{
			client.ui.refreshGameConfig(gameCfg);
		}

		/* (non-Javadoc)
		 * @see common.Protocol.Client#receiveRunningGameMessage(common.Player, java.lang.String)
		 */
		@Override
		public void receiveRunningGameMessage(Player fromPlayer, String msg) throws RpcException
		{
			client.ui.receiveRunningGameMessage(fromPlayer, msg);
		}

		@Override
		public void receiveNewTurnGameBoard(PlayerGameBoard gameBoard) throws RpcException
		{
			client.ui.receiveNewTurnGameBoard(gameBoard);
		}

		@Override
		public void receivePausedGameMessage(Player fromPlayer, String msg) throws RpcException
		{
			client.ui.receivePausedGameMessage(fromPlayer, msg);
		}
		
	}
	
	private final GameClient client;
	private final IUserInterface ui;
	
	public SEPClient(IUserInterface ui, String login, String server, int port, long timeOut)
	{
		this(ui, login, "", server, port, timeOut);
	}
	
	public SEPClient(IUserInterface ui, String login, String pwd, String server, int port, long timeOut)
	{
		this.ui = ui;
		this.client = new GameClient(ui, login, pwd, server, port, timeOut);
		try
		{
			this.client.registerClientCommandExecutor(Protocol.Client.class, new SEPClientProtocol(this));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error(e);
		}
	}

	public void connect()
	{
		client.connect();
		log.log(Level.INFO, "Client '"+getLogin()+"' connected");
	}
	
	public void disconnect()
	{
		log.log(Level.INFO, "Client '"+getLogin()+"' disconnecting");
		client.disconnect();
	}
	
	public boolean isConnected()
	{
		return client.isConnected();
	}
	
	/**
	 * Return last login used to connect.
	 * @return String last login used to connect.
	 */
	public String getLogin()
	{		
		return client.getLogin();
	}
	
	public void runGame() throws ServerPrivilegeException, StateMachineNotExpectedEventException, RpcException
	{
		client.getServerInterface().runGame();
	}
	
	public void pauseGame() throws StateMachineNotExpectedEventException, ServerPrivilegeException, RpcException
	{
		client.getServerInterface().pauseGame();
	}

	public void resumeGame() throws StateMachineNotExpectedEventException, ServerPrivilegeException, RpcException
	{
		client.getServerInterface().resumeGame();
	}
	
	public boolean isGameSaved(String savedGameId) throws ServerPrivilegeException, ServerSavingGameException, StateMachineNotExpectedEventException, RpcException
	{
		return client.getServerInterface().isGameSaved(savedGameId);
	}
	
	public void loadGame(String savedGameId) throws ServerPrivilegeException, StateMachineNotExpectedEventException, RpcException
	{
		client.getServerInterface().loadGame(savedGameId);
	}
	
	public void saveGame(String saveGameId) throws ServerPrivilegeException, StateMachineNotExpectedEventException, RpcException
	{
		client.getServerInterface().saveGame(saveGameId);
	}
	
	private Protocol.ServerGameCreation gameCreationProxy = null;
	public Protocol.ServerGameCreation getGameCreationInterface() throws RpcException
	{
		if (!isConnected()) throw new IllegalStateException("Not connected.");
		
		if (gameCreationProxy == null)
		{
			gameCreationProxy = client.getCustomServerInterface(Protocol.ServerGameCreation.class);			
		}
		return gameCreationProxy;
	}

	private Protocol.ServerRunningGame runningGameProxy = null;
	public Protocol.ServerRunningGame getRunningGameInterface() throws RpcException
	{
		if (!isConnected()) throw new IllegalStateException("Not connected.");
		
		if (runningGameProxy == null)
		{
			runningGameProxy = (Protocol.ServerRunningGame) client.getCustomServerInterface(Protocol.ServerRunningGame.class);			
		}
		return runningGameProxy;
	}

	private Protocol.ServerPausedGame pausedGameProxy = null;
	public Protocol.ServerPausedGame getPausedGameInterface() throws RpcException
	{
		if (!isConnected()) throw new IllegalStateException("Not connected.");
		
		if (pausedGameProxy == null)
		{
			pausedGameProxy = client.getCustomServerInterface(Protocol.ServerPausedGame.class);
		}
		return pausedGameProxy;
	}
}
