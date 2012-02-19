package org.axan.sep.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.axan.eplib.clientserver.ftp.FTPClient;
import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.client.GameClient;
import org.axan.eplib.gameserver.common.IClientUser;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.eplib.gameserver.common.IServerUser.ServerSavingGameException;
import org.axan.eplib.orm.sql.ISQLDataBase;
import org.axan.eplib.orm.sql.ISQLDataBaseFactory;
import org.axan.eplib.orm.sql.SQLDataBaseException;
import org.axan.eplib.orm.sql.sqlite.SQLiteDB;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.Protocol.SEPImplementationError;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.axan.sep.common.db.IGameEvent;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;
import org.axan.sep.common.db.IDBFactory;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.orm.SEPCommonDB;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 * 
 */
public class SEPClient implements IDBFactory
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
		void refreshPlayerList(Map<String, IPlayerConfig> playerList);

		/**
		 * New GameCreation message received from another player.
		 * Must return fast.
		 * @param fromPlayer
		 * @param msg
		 */
		void receiveGameCreationMessage(String fromPlayer, String msg);

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
		void receiveRunningGameMessage(String fromPlayer, String msg);

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
		void receivePausedGameMessage(String fromPlayer, String msg);
		
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
		public void refreshPlayerList(final Map<String, IPlayerConfig> playerList) throws RpcException
		{
			client.getGameboard().refreshPlayerList(playerList);
			
			SwingUtilities.invokeLater(new Runnable()
			{
				
				@Override
				public void run()
				{
					client.ui.refreshPlayerList(playerList);
				}
			});
		}

		/* (non-Javadoc)
		 * @see common.Protocol.Client#receiveGameCreationMessage(common.Player, java.lang.String)
		 */
		@Override
		public void receiveGameCreationMessage(final String fromPlayer, final String msg)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				
				@Override
				public void run()
				{
					client.ui.receiveGameCreationMessage(fromPlayer, msg);
				}
			});
		}

		/* (non-Javadoc)
		 * @see common.Protocol.Client#refreshGameConfig(common.GameConfig)
		 */
		@Override
		public void refreshGameConfig(final GameConfig gameCfg)
		{
			client.getGameboard().refreshGameConfig(gameCfg);
			
			SwingUtilities.invokeLater(new Runnable()
			{
				
				@Override
				public void run()
				{
					client.ui.refreshGameConfig(gameCfg);
				}
			});
		}

		/* (non-Javadoc)
		 * @see common.Protocol.Client#receiveRunningGameMessage(common.Player, java.lang.String)
		 */
		@Override
		public void receiveRunningGameMessage(final String fromPlayer, final String msg) throws RpcException
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				
				@Override
				public void run()
				{
					client.ui.receiveRunningGameMessage(fromPlayer, msg);
				}
			});
		}

		@Override
		public void receiveNewTurnGameBoard(final List<IGameEvent> newTurnEvents) throws RpcException
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
			
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					client.ui.receiveNewTurnGameBoard(newTurnEvents);
				};
			});
		}

		@Override
		public void receivePausedGameMessage(final String fromPlayer, final String msg) throws RpcException
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				
				@Override
				public void run()
				{
					client.ui.receivePausedGameMessage(fromPlayer, msg);
				}
			});
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
	
	public void endTurn() throws StateMachineNotExpectedEventException, GameCommandException, RpcException, GameBoardException
	{
		getRunningGameInterface().endTurn(getGameboard().endTurn());
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
	public GraphDatabaseService createDB()
	{
		try
		{
			File dbDirectory = File.createTempFile("sepC", "");
			dbDirectory.delete();
			dbDirectory.mkdir();
			final GraphDatabaseService db = new EmbeddedGraphDatabase(dbDirectory.getAbsolutePath());
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					db.shutdown();
				}
			});
			
			return db;
		}
		catch(Throwable t)
		{
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
