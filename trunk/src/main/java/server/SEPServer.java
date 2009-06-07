/**
 * @author Escallier Pierre
 * @file SEPServer.java
 * @date 23 juin 08
 */
package server;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.IServer;
import org.axan.eplib.clientserver.ServerClient;
import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.clientserver.rpc.RpcServerModule.RpcServerModuleBadServiceException;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.eplib.gameserver.server.GameServer;
import org.axan.eplib.gameserver.server.GameServer.ExecutorFactory;
import org.axan.eplib.gameserver.server.GameServer.GameServerAlreadyRegisteredExecutorInterfaceException;
import org.axan.eplib.gameserver.server.GameServer.GameServerListener;
import org.axan.eplib.gameserver.server.GameServer.ServerUser;
import org.axan.eplib.statemachine.ProxiedStateMachine.ProxiedStateMachineBadServiceException;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.eplib.utils.Basic;

import server.model.ServerGame;

import common.GameConfig;
import common.PlayerGameBoard;
import common.Player;
import common.PlayerConfig;
import common.Protocol;
import common.Protocol.ServerGameCreation;
import common.Protocol.ServerPausedGame;
import common.Protocol.ServerRunningGame;

/**
 * TODO
 */
public class SEPServer implements IServer, GameServerListener
{

	public static final Logger		log	= Logger.getLogger(SEPServer.class.getCanonicalName());

	private final ExecutorService	threadPool;

	/**
	 * ServerCommon protocol interface implementation.
	 */
	private static class SEPCommon implements Protocol.ServerCommon
	{
		protected final SEPServer	sepServer;

		protected final ServerUser	user;

		/**
		 * Full constructor.
		 * 
		 * @param server
		 *            Current server.
		 * @param user
		 *            Current user.
		 */
		public SEPCommon(SEPServer server, ServerUser user)
		{
			this.sepServer = server;
			this.user = user;
		}

		protected Player getPlayer()
		{
			return sepServer.players.get(user.getLogin()).getPlayer();
		}

		protected ServerPlayer getServerPlayer()
		{
			return sepServer.players.get(user.getLogin());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.Protocol.ServerCommon#getPlayerList()
		 */
		@Override
		public Set<Player> getPlayerList()
		{
			return sepServer.getPlayerList();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.Protocol.ServerCommon#getGameConfig()
		 */
		@Override
		public GameConfig getGameConfig() throws RpcException, StateMachineNotExpectedEventException
		{
			return sepServer.gameConfig;
		}

	}

	/**
	 * ServerGameCreation protocol interface implementation.
	 */
	private static class SEPGameCreation extends SEPCommon implements Protocol.ServerGameCreation
	{

		/**
		 * Full constructor.
		 * 
		 * @param server
		 *            Current server.
		 * @param user
		 *            Current user.
		 */
		public SEPGameCreation(SEPServer server, ServerUser user)
		{
			super(server, user);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.Protocol.ServerGameCreation#sendMessage(java.lang.String)
		 */
		@Override
		public void sendMessage(final String msg)
		{
			sepServer.threadPool.execute(new Runnable()
			{

				@Override
				public void run()
				{
					sepServer.doForEachConnectedPlayer(new DoItToOnePlayer()
					{

						@Override
						public void doIt(ServerPlayer player)
						{
							try
							{
								player.getClientInterface().receiveGameCreationMessage(getPlayer(), msg);
							}
							catch (RpcException e)
							{
								log.log(Level.WARNING, "RpcException(" + player.getName() + ") : " + e.getMessage());
								player.abort(e);
							}
						}
					});
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.Protocol.ServerGameCreation#updateGameConfig(common.GameConfig)
		 */
		@Override
		public void updateGameConfig(GameConfig gameCfg) throws ServerPrivilegeException
		{
			if ( !user.isAdmin())
			{
				throw new ServerPrivilegeException("Only admin can update game config.");
			}

			synchronized (sepServer.gameConfig)
			{
				sepServer.gameConfig = gameCfg;
				sepServer.threadPool.execute(new Runnable()
				{

					@Override
					public void run()
					{
						sepServer.refreshGameConfig();
					}
				});
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.Protocol.ServerGameCreation#updatePlayerConfig(common.PlayerConfig)
		 */
		@Override
		public void updatePlayerConfig(final PlayerConfig playerCfg)
		{
			sepServer.threadPool.execute(new Runnable()
			{

				@Override
				public void run()
				{
					getServerPlayer().setConfig(playerCfg);
					sepServer.refreshPlayerList();
				}
			});
		}

	}

	private static class SEPRunningGame extends SEPCommon implements Protocol.ServerRunningGame
	{

		/**
		 * Full constructor.
		 * 
		 * @param server
		 *            Current server.
		 * @param user
		 *            Current user.
		 */
		public SEPRunningGame(SEPServer server, ServerUser user)
		{
			super(server, user);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.Protocol.ServerRunningGame#getGameTurn()
		 */
		@Override
		public PlayerGameBoard getGameBoard() throws RpcException, StateMachineNotExpectedEventException
		{
			return sepServer.getCurrentGame().getGameBoard(user.getLogin());
		}

		/* (non-Javadoc)
		 * @see common.Protocol.ServerRunningGame#sendMessage(java.lang.String)
		 */
		@Override
		public void sendMessage(final String msg) throws RpcException, StateMachineNotExpectedEventException
		{
			sepServer.threadPool.execute(new Runnable()
			{

				@Override
				public void run()
				{
					// TODO : Filter running game message according to pulsar effect.
					sepServer.doForEachConnectedPlayer(new DoItToOnePlayer()
					{

						@Override
						public void doIt(ServerPlayer player)
						{
							try
							{
								player.getClientInterface().receiveRunningGameMessage(getPlayer(), msg);
							}
							catch (RpcException e)
							{
								log.log(Level.WARNING, "RpcException(" + player.getName() + ") : " + e.getMessage());
								player.abort(e);
							}
						}
					});
				}
			});
		}

	}

	private static class SEPPausedGame extends SEPCommon implements Protocol.ServerPausedGame
	{
		/**
		 * Full constructor.
		 * 
		 * @param server
		 *            Current server.
		 * @param user
		 *            Current user.
		 */
		public SEPPausedGame(SEPServer server, ServerUser user)
		{
			super(server, user);
		}
	}

	private final GameServer				gameServer;

	private final Map<String, ServerPlayer>	players		= new HashMap<String, ServerPlayer>();

	private GameConfig						gameConfig	= new GameConfig();

	private ServerGame						game		= null;

	public SEPServer(int port, long timeOut)
	{
		gameServer = new GameServer(this, port, timeOut);
		try
		{
			gameServer.registerGameCreationCommandExecutorFactory(Protocol.ServerGameCreation.class, new ExecutorFactory<Protocol.ServerGameCreation>()
			{

				@Override
				public ServerGameCreation create(ServerUser user)
				{
					return new SEPGameCreation(SEPServer.this, user);
				}
			});

			gameServer.registerRunningGameCommandExecutorFactory(Protocol.ServerRunningGame.class, new ExecutorFactory<Protocol.ServerRunningGame>()
			{

				@Override
				public ServerRunningGame create(ServerUser user)
				{
					return new SEPRunningGame(SEPServer.this, user);
				}
			});

			gameServer.registerPausedGameCommandExecutorFactory(Protocol.ServerPausedGame.class, new ExecutorFactory<Protocol.ServerPausedGame>()
			{

				@Override
				public ServerPausedGame create(ServerUser user)
				{
					return new SEPPausedGame(SEPServer.this, user);
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error(e);
		}
		threadPool = Executors.newCachedThreadPool();
	}

	/**
	 * @return
	 */
	public Set<Player> getPlayerList()
	{
		Set<Player> result = new HashSet<Player>();

		synchronized (players)
		{
			for (String name : players.keySet())
			{
				ServerPlayer player = players.get(name);

				if (player != null && player.isConnected())
				{
					result.add(player.getPlayer());
				}
			}
		}

		return result;
	}

	private ServerGame getCurrentGame()
	{
		synchronized (this)
		{
			if (game == null)
			{
				game = new ServerGame(getPlayerList(), gameConfig);
			}
		}
		return game;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.eplib.clientserver.IServer#getAddress()
	 */
	@Override
	public InetAddress getAddress()
	{
		return gameServer.getAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.eplib.clientserver.IServer#getClientsNumber()
	 */
	@Override
	public int getClientsNumber()
	{
		return gameServer.getClientsNumber();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.eplib.clientserver.IServer#start()
	 */
	@Override
	public void start()
	{
		gameServer.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.eplib.clientserver.IServer#stop()
	 */
	@Override
	public void stop()
	{
		gameServer.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.eplib.clientserver.IServer#terminate()
	 */
	@Override
	public void terminate()
	{
		gameServer.terminate();
		threadPool.shutdown();
	}

	public String getServerAdminKey()
	{
		return gameServer.getServerAdminKey();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.eplib.gameserver.server.GameServer.GameServerListener#playerLoggedIn(org.axan.eplib.gameserver.server.GameServer.ServerUser)
	 */
	@Override
	public void playerLoggedIn(ServerUser player)
	{
		synchronized (players)
		{
			if (players.containsKey(player.getLogin()))
			{
				players.get(player.getLogin()).setServerUser(player);
			}
			else
			{
				players.put(player.getLogin(), new ServerPlayer(player));
			}
		}
		refreshPlayerList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.eplib.gameserver.server.GameServer.GameServerListener#playerLoggedOut(org.axan.eplib.gameserver.server.GameServer.ServerUser)
	 */
	@Override
	public void playerLoggedOut(ServerUser player)
	{
		if (players.containsKey(player.getLogin()))
		{
			players.get(player.getLogin()).setServerUser(null);
			refreshPlayerList();
		}
	}

	private void refreshGameConfig()
	{
		doForEachConnectedPlayer(new DoItToOnePlayer()
		{

			@Override
			public void doIt(ServerPlayer player)
			{
				try
				{
					player.getClientInterface().refreshGameConfig(gameConfig);
				}
				catch (RpcException e)
				{
					log.log(Level.WARNING, "RpcException(" + player.getName() + ") : " + e.getMessage());
				}
			}
		});
	}

	private void refreshPlayerList()
	{
		final Set<Player> playerList = getPlayerList();

		doForEachConnectedPlayer(new DoItToOnePlayer()
		{

			@Override
			public void doIt(ServerPlayer player)
			{
				try
				{
					player.getClientInterface().refreshPlayerList(playerList);
				}
				catch (RpcException e)
				{
					log.log(Level.WARNING, "RpcException(" + player.getName() + ") : " + e.getMessage());
				}
			}
		});
	}

	private static interface DoItToOnePlayer
	{
		void doIt(ServerPlayer player);
	}

	private void doForEachConnectedPlayer(DoItToOnePlayer doIt)
	{
		synchronized (players)
		{
			for (String login : players.keySet())
			{
				ServerPlayer player = players.get(login);
				if (player != null && player.isConnected())
				{
					doIt.doIt(player);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.eplib.gameserver.server.GameServer.GameServerListener#gameRan()
	 */
	@Override
	public void gameRan()
	{
		log.log(Level.INFO, "gameRan");
		threadPool.execute(new Runnable()
		{
		
			@Override
			public void run()
			{
				getCurrentGame();		
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.eplib.gameserver.server.GameServer.GameServerListener#gamePaused()
	 */
	@Override
	public void gamePaused()
	{
		log.log(Level.INFO, "gamePaused");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.eplib.gameserver.server.GameServer.GameServerListener#gameResumed()
	 */
	@Override
	public void gameResumed()
	{
		log.log(Level.INFO, "gameResumed");
	}
}
