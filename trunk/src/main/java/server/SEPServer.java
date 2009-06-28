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

import server.model.GameBoard;
import server.model.PlayerGameMove;
import server.model.ServerGame;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import common.GameConfig;
import common.IBuilding;
import common.IStarship;
import common.PlayerGameBoard;
import common.Player;
import common.PlayerConfig;
import common.Protocol;
import common.Protocol.ServerGameCreation;
import common.Protocol.ServerPausedGame;
import common.Protocol.ServerRunningGame;
import common.Protocol.ServerRunningGame.RunningGameCommandException;

/**
 * TODO
 */
public class SEPServer implements IServer, GameServerListener
{

	public static final Logger		log	= Logger.getLogger(SEPServer.class.getCanonicalName());

	private final ExecutorService	threadPool;

	public static class SEPImplementationException extends Error
	{
		public SEPImplementationException(String msg)
		{
			super(msg);
		}
		
		public SEPImplementationException(String msg, Throwable t)
		{
			super(msg, t);
		}
	}
	
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

		protected String getLogin()
		{
			return user.getLogin();
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
							catch(RpcException e)
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
			if (!user.isAdmin())
			{
				throw new ServerPrivilegeException("Only admin can update game config.");
			}

			synchronized(sepServer.gameConfig)
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

		private GameBoard getGameBoard()
		{
			return sepServer.getCurrentGame().getGameBoard(user.getLogin());
		}

		private PlayerGameMove getGameMove()
		{
			return sepServer.getCurrentGame().getPlayerGameMove(user.getLogin());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.Protocol.ServerRunningGame#getPlayerGameBoard()
		 */
		@Override
		public PlayerGameBoard getPlayerGameBoard() throws RpcException, StateMachineNotExpectedEventException
		{
			return getGameBoard().getPlayerGameBoard(user.getLogin());
		}

		@Override
		public void canSendMessage(String msg) throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO
			throw new NotImplementedException();
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
							catch(RpcException e)
							{
								log.log(Level.WARNING, "RpcException(" + player.getName() + ") : " + e.getMessage());
								player.abort(e);
							}
						}
					});
				}
			});
		}

		@Override
		public void canAttackEnemiesFleet() throws RpcException, StateMachineNotExpectedEventException
		{
			// if (getGameMove().isTurnEnded()) return false;
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void attackEnemiesFleet(String celestialBodyName) throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public boolean canBuild(String celestialBodyName, Class<? extends common.IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return false;
			return getGameBoard().canBuild(getLogin(), celestialBodyName, buildingType);
		}

		@Override
		public void build(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addBuildCommand(celestialBodyName, buildingType);
		}

		@Override
		public void canBuildSpaceRoad() throws RpcException, StateMachineNotExpectedEventException
		{
			// if (getGameMove().isTurnEnded()) return false;
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void buildSpaceRoad(String celestialBodyNameA, String celestialBodyNameB) throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void canChangeConquestPolicy() throws RpcException, StateMachineNotExpectedEventException
		{
			// if (getGameMove().isTurnEnded()) return false;
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void changeConquestPolicy() throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void canChangeDomesticPolicy() throws RpcException, StateMachineNotExpectedEventException
		{
			// if (getGameMove().isTurnEnded()) return false;
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void changeDomesticPolicy() throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public boolean canDemolish(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return false;
			return getGameBoard().canDemolish(getLogin(), celestialBodyName, buildingType);
		}

		@Override
		public void demolish(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addDemolishCommand(celestialBodyName, buildingType);
		}

		@Override
		public void canDemolishSpaceRoad() throws RpcException, StateMachineNotExpectedEventException
		{
			// if (getGameMove().isTurnEnded()) return false;
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void demolishSpaceRoad(String celestialBodyNameA, String celestialBodyNameB) throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public boolean canDismantleFleet(String fleetName) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return false;
			return getGameBoard().canDismantleFleet(getLogin(), fleetName);
		}

		@Override
		public void dismantleFleet(String fleetName) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addDismantleFleetCommand(fleetName);
		}

		@Override
		public boolean canEmbarkGovernment() throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return false;
			return getGameBoard().canEmbarkGovernment(getLogin());
		}

		@Override
		public void embarkGovernment() throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addEmbarkGovernmentCommand(getLogin());
		}

		@Override
		public boolean canFirePulsarMissile(String celestialBodyName) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return false;
			return getGameBoard().canFirePulsarMissile(getPlayer(), celestialBodyName);			
		}

		@Override
		public void firePulsarMissile(String celestialBodyName, float bonusModifier) throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}
		
		@Override
		public boolean canFormFleet(String planetName, String fleetName, Map<Class<? extends IStarship>, Integer> fleetToForm) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return false;
			return getGameBoard().canFormFleet(getLogin(), planetName, fleetName, fleetToForm);
		}

		@Override
		public void formFleet(String planetName, String fleetName, Map<Class<? extends IStarship>, Integer> composition) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addFormFleetCommand(planetName, fleetName, composition);
		}

		@Override
		public void canLaunchProbe() throws RpcException, StateMachineNotExpectedEventException
		{
			// if (getGameMove().isTurnEnded()) return false;
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void launchProbe(String probeName, int[] destination) throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public boolean canMakeStarships(String planetName, Map<Class<? extends IStarship>, Integer> starshipsToMake) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return false;
			return getGameBoard().canMakeStarships(getLogin(), planetName, starshipsToMake);
		}

		@Override
		public void makeStarships(String planetName, Map<Class<? extends IStarship>, Integer> starshipsToMake) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addMakeStarshipsCommand(planetName, starshipsToMake);
		}

		@Override
		public void canModifyCarbonOrder() throws RpcException, StateMachineNotExpectedEventException
		{
			// if (getGameMove().isTurnEnded()) return false;
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void modifyCarbonOrder(String originCelestialBodyName, String destinationCelestialBodyName, int amount) throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void canMoveFleet() throws RpcException, StateMachineNotExpectedEventException
		{
			// if (getGameMove().isTurnEnded()) return false;
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void moveFleet(String fleetName, int delay, Set<String> checkpoints) throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public boolean canSettleGovernment(String planetName) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return false;
			return getGameBoard().canSettleGovernment(getPlayer(), planetName);
		}

		@Override
		public void settleGovernment() throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}		
		
		@Override
		public void endTurn() throws RpcException, StateMachineNotExpectedEventException
		{
			getGameMove().endTurn();
			
			sepServer.threadPool.execute(new Runnable()
			{

				@Override
				public void run()
				{
					sepServer.checkForNextTurn();
				}
			});
		}

		@Override
		public void resetTurn() throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().resetTurn();			
		}

		@Override
		public boolean canEndTurn() throws RpcException, StateMachineNotExpectedEventException
		{
			return !getGameMove().isTurnEnded();
		}

		@Override
		public boolean canResetTurn() throws RpcException, StateMachineNotExpectedEventException
		{
			return !getGameMove().isTurnEnded();
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

		@Override
		public Map<Player, Boolean> getPlayerStateList() throws RpcException, StateMachineNotExpectedEventException
		{
			return sepServer.getPlayerStateList();
		}

		@Override
		public void sendMessage(final String msg) throws RpcException, StateMachineNotExpectedEventException
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
								player.getClientInterface().receivePausedGameMessage(getPlayer(), msg);
							}
							catch(RpcException e)
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
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Error(e);
		}
		threadPool = Executors.newCachedThreadPool();
	}

	public Map<Player, Boolean> getPlayerStateList()
	{
		Map<Player, Boolean> result = new HashMap<Player, Boolean>();

		synchronized(players)
		{
			for(String name : players.keySet())
			{
				ServerPlayer player = players.get(name);

				if (player != null)
				{
					result.put(player.getPlayer(), player.isConnected());
				}				
			}
		}

		return result;
	}

	private void checkForNextTurn()
	{
		log.log(Level.SEVERE, "Checking for next turn...");
		
		synchronized(players)
		{
			for(String name : players.keySet())
			{
				if (!getCurrentGame().getPlayerGameMove(name).isTurnEnded()) return;
			}						
		}
		
		// TODO : Resolve turn;
		getCurrentGame().resolveCurrentTurn();
		
		refreshPlayerGameBoards();
	}
	
	private void refreshPlayerGameBoards()
	{
		synchronized(players)
		{
			for(String name : players.keySet())
			{
				try
				{
					players.get(name).getClientInterface().receiveNewTurnGameBoard(getCurrentGame().getGameBoard(name).getPlayerGameBoard(name));
				}
				catch(RpcException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @return
	 */
	public Set<Player> getPlayerList()
	{
		Set<Player> result = new HashSet<Player>();

		synchronized(players)
		{
			for(String name : players.keySet())
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
		synchronized(this)
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
		synchronized(players)
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
				catch(RpcException e)
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
				catch(RpcException e)
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
		synchronized(players)
		{
			for(String login : players.keySet())
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
