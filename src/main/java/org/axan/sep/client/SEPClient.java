package org.axan.sep.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.ftp.FTPClient;
import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.client.GameClient;
import org.axan.eplib.gameserver.common.IClientUser;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.eplib.gameserver.common.IServerUser.ServerSavingGameException;
import org.axan.eplib.orm.ISQLDataBase;
import org.axan.eplib.orm.ISQLDataBaseFactory;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.db.IGameEvent;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.PlayerConfig;

/**
 * 
 */
public class SEPClient implements ISQLDataBaseFactory
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
		void refreshPlayerList(Map<IPlayer, IPlayerConfig> playerList);

		/**
		 * New GameCreation message received from another player.
		 * Must return fast.
		 * @param fromPlayer
		 * @param msg
		 */
		void receiveGameCreationMessage(IPlayer fromPlayer, String msg);

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
		void receiveRunningGameMessage(IPlayer fromPlayer, String msg);

		/**
		 * New turn has been sent from the server. Client must refresh the gameboard.
		 * @param gameBoard
		 */
		void receiveNewTurnGameBoard(List<IGameEvent> newTurnEvents);

		/**
		 * New PausedGame message received from another player.
		 * Must return fast.
		 * @param fromPlayer
		 * @param msg
		 */
		void receivePausedGameMessage(IPlayer fromPlayer, String msg);
		
		/**
		 * Return true if current client is supposed to be game admin (i.e. server host).
		 * @return
		 */
		boolean isAdmin();
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
		public void refreshPlayerList(Map<IPlayer, IPlayerConfig> playerList) throws RpcException
		{
			client.getGameboard().refreshPlayerList(playerList);
			client.ui.refreshPlayerList(playerList);
		}

		/* (non-Javadoc)
		 * @see common.Protocol.Client#receiveGameCreationMessage(common.Player, java.lang.String)
		 */
		@Override
		public void receiveGameCreationMessage(IPlayer fromPlayer, String msg)
		{
			client.ui.receiveGameCreationMessage(fromPlayer, msg);
		}

		/* (non-Javadoc)
		 * @see common.Protocol.Client#refreshGameConfig(common.GameConfig)
		 */
		@Override
		public void refreshGameConfig(GameConfig gameCfg)
		{
			client.getGameboard().refreshGameConfig(gameCfg);
			client.ui.refreshGameConfig(gameCfg);
		}

		/* (non-Javadoc)
		 * @see common.Protocol.Client#receiveRunningGameMessage(common.Player, java.lang.String)
		 */
		@Override
		public void receiveRunningGameMessage(IPlayer fromPlayer, String msg) throws RpcException
		{
			client.ui.receiveRunningGameMessage(fromPlayer, msg);
		}

		@Override
		public void receiveNewTurnGameBoard(List<IGameEvent> newTurnEvents) throws RpcException
		{
			try
			{
				client.getGameboard().receiveNewTurnGameBoard(newTurnEvents);
			}
			catch(GameBoardException e)
			{
				log.log(Level.SEVERE, "Error resolving turn, try to re-sync game", e);
				client.syncGame();
				return;
			}
			
			client.ui.receiveNewTurnGameBoard(newTurnEvents);
		}

		@Override
		public void receivePausedGameMessage(IPlayer fromPlayer, String msg) throws RpcException
		{
			client.ui.receivePausedGameMessage(fromPlayer, msg);
		}
		
	}
	
	private final GameClient client;
	private final IUserInterface ui;
	private final PlayerGameBoard gameBoard;	
	
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
		
		gameBoard = new PlayerGameBoard(this);
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
	
	public String getPassword()
	{
		return client.getPassword();
	}
	
	public boolean isAdmin()
	{
		return ui.isAdmin();
	}
	
	public String getServer()
	{
		return client.getServer();
	}
	
	public FTPClient getFTPClient()
	{
		return client.getFTPClient();
	}
	
	public void uploadFile(String localFilepath, String remoteFilename) throws IOException
	{
		client.uploadFile(localFilepath, remoteFilename);
	}
	
	public URL getUserDirectoryURL(String username)
	{
		return client.getUserDirectoryURL(username);
	}
	
	public URL getHomeDirectoryURL()
	{
		return client.getHomeDirectoryURL();
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
	
	public boolean isGameSaved(String savedGameId) throws ServerPrivilegeException, StateMachineNotExpectedEventException, RpcException, ServerSavingGameException
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
			runningGameProxy = client.getCustomServerInterface(Protocol.ServerRunningGame.class);			
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
	
	@Override
	public ISQLDataBase createSQLDataBase()
	{
		try
		{
			//TODO: When DB debug is finished, use memory DB (no arg constructor).
			File dbFile = File.createTempFile("sepC", ".sep");
			return new SQLiteDB(dbFile);
			//return new HSQLDB(dbFile, "sa", "");			
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			log.log(Level.SEVERE, t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}
	
	public PlayerGameBoard getGameboard()
	{
		return gameBoard;
	}
	
	public void sendMessage(String msg) throws StateMachineNotExpectedEventException, RpcException
	{
		if (getGameboard().isGameInCreation())
		{
			getGameCreationInterface().sendMessage(msg);
		}
		else
		{
			// TODO: Check for paused game
			getRunningGameInterface().sendMessage(msg);
		}
	}
	
	////////// private methods
	
	private void syncGame()
	{
		throw new RuntimeException("Game re-sync not implemented yet");
	}
}
