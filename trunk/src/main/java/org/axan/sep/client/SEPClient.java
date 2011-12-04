package org.axan.sep.client;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.ftp.FTPClient;
import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.client.GameClient;
import org.axan.eplib.gameserver.common.IClientUser;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.eplib.gameserver.common.IServerUser.ServerSavingGameException;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.eplib.utils.Operations.INotParameterisedOperationWithReturn;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.Player;
import org.axan.sep.common.db.orm.PlayerConfig;

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
		void refreshPlayerList(Map<Player, PlayerConfig> playerList);

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
		public void refreshPlayerList(Map<Player, PlayerConfig> playerList) throws RpcException
		{
			client.refreshPlayerList(playerList);
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
			client.setGameBoard(gameBoard);
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
	private PlayerGameBoard gameBoard=null;
	
	private final Map<String, IPlayerConfig> playerConfigs = new HashMap<String, IPlayerConfig>();
	
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
	
	public boolean isRunning()
	{
		return getGameBoard() != null;
	}
	
	private synchronized void refreshPlayerList(Map<Player, PlayerConfig> playerList)
	{
		if (!isRunning())
		{
			playerConfigs.clear();
			for(IPlayerConfig pc : playerList.values())
			{
				playerConfigs.put(pc.getName(), pc);
			}
		}
	}
	
	/**
	 * Get player config no matter the game current state (in creation or running)
	 * @param playerName
	 * @return
	 * @throws SQLDataBaseException 
	 */
	public synchronized IPlayerConfig getPlayerConfig(String playerName) throws SQLDataBaseException
	{
		if (!isRunning())
		{
			return playerConfigs.get(playerName);
		}
		else
		{
			return PlayerConfig.selectOne(getDB(), PlayerConfig.class, null, "name = %s", playerName);
		}		
	}
	
	public void setGameBoard(PlayerGameBoard gameBoard)
	{
		this.gameBoard = gameBoard;
	}
	
	public PlayerGameBoard getGameBoard()
	{
		return gameBoard;
	}
	
	public SEPCommonDB getDB()
	{
		return gameBoard==null?null:gameBoard.getDB();
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
