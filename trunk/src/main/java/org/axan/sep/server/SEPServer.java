/**
 * @author Escallier Pierre
 * @file SEPServer.java
 * @date 23 juin 08
 */
package org.axan.sep.server;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
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
import org.axan.sep.common.CarbonOrder;
import org.axan.sep.common.CommandCheckResult;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.IBuilding;
import org.axan.sep.common.Player;
import org.axan.sep.common.PlayerConfig;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.Diplomacy.PlayerPolicies;
import org.axan.sep.common.Protocol.ServerGameCreation;
import org.axan.sep.common.Protocol.ServerPausedGame;
import org.axan.sep.common.Protocol.ServerRunningGame;
import org.axan.sep.common.Protocol.ServerRunningGame.RunningGameCommandException;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.model.GameBoard;
import org.axan.sep.server.model.PlayerGameMove;
import org.axan.sep.server.model.ServerGame;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * TODO
 */
public class SEPServer implements IServer
{

	public static Logger		log	= Logger.getLogger(SEPServer.class.getCanonicalName());

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
		SEPCommon(SEPServer server, ServerUser user)
		{
			this.sepServer = server;
			this.user = user;
		}

		String getLogin()
		{
			return user.getLogin();
		}

		Player getPlayer()
		{
			return sepServer.players.get(user.getLogin()).getPlayer();
		}

		ServerPlayer getServerPlayer()
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
	static class SEPGameCreation extends SEPCommon implements Protocol.ServerGameCreation
	{

		/**
		 * Full constructor.
		 * 
		 * @param server
		 *            Current server.
		 * @param user
		 *            Current user.
		 */
		SEPGameCreation(SEPServer server, ServerUser user)
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
				log.log(Level.WARNING, user.getLogin()+" tried to update game config but is not admin.");
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
		SEPRunningGame(SEPServer server, ServerUser user)
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
		public CommandCheckResult canSendMessage(String msg) throws RpcException, StateMachineNotExpectedEventException
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
		public CommandCheckResult canAttackEnemiesFleet(String celestialBodyName) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canAttackEnemiesFleet(getLogin(), celestialBodyName);
		}

		@Override
		public void attackEnemiesFleet(String celestialBodyName) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addAttackEnemiesFleetCommand(celestialBodyName);
		}

		@Override
		public CommandCheckResult canBuild(String celestialBodyName, Class<? extends org.axan.sep.common.IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canBuild(getLogin(), celestialBodyName, buildingType);
		}

		@Override
		public void build(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addBuildCommand(celestialBodyName, buildingType);
		}

		@Override
		public CommandCheckResult canBuildSpaceRoad(String sourceName, String destinationName) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canBuildSpaceRoad(getLogin(), sourceName, destinationName);
		}

		@Override
		public void buildSpaceRoad(String sourceName, String destinationName) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addBuildSpaceRoadCommand(sourceName, destinationName);
		}

		@Override
		public CommandCheckResult canChangeDiplomacy(Map<String, PlayerPolicies> newPolicies) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canChangeDiplomacy(getLogin(), newPolicies);
		}

		@Override
		public void changeDiplomacy(Map<String, PlayerPolicies> newPolicies) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addChangeDiplomacyCommand(newPolicies);
		}

		@Override
		public CommandCheckResult canDemolish(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canDemolish(getLogin(), celestialBodyName, buildingType);
		}

		@Override
		public void demolish(String celestialBodyName, Class<? extends IBuilding> buildingType) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addDemolishCommand(celestialBodyName, buildingType);
		}

		@Override
		public CommandCheckResult canDemolishSpaceRoad(String sourceName, String destinationName) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canDemolishSpaceRoad(getLogin(), sourceName, destinationName);
		}

		@Override
		public void demolishSpaceRoad(String sourceName, String destinationName) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addDemolishSpaceRoadCommand(sourceName, destinationName);
		}

		@Override
		public CommandCheckResult canDismantleFleet(String fleetName) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canDismantleFleet(getLogin(), fleetName);
		}

		@Override
		public void dismantleFleet(String fleetName) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addDismantleFleetCommand(fleetName);
		}

		@Override
		public CommandCheckResult canEmbarkGovernment() throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canEmbarkGovernment(getLogin());
		}

		@Override
		public void embarkGovernment() throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addEmbarkGovernmentCommand(getLogin());
		}

		@Override
		public CommandCheckResult canFirePulsarMissile(String celestialBodyName) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canFirePulsarMissile(getLogin(), celestialBodyName);
		}

		@Override
		public void firePulsarMissile(String celestialBodyName, float bonusModifier) throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public CommandCheckResult canFormFleet(String planetName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<org.axan.sep.common.ISpecialUnit> fleetToFormSpecialUnits) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canFormFleet(getLogin(), planetName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits);
		}

		@Override
		public void formFleet(String planetName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<org.axan.sep.common.ISpecialUnit> fleetToFormSpecialUnits) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addFormFleetCommand(planetName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits);
		}

		@Override
		public CommandCheckResult canFireAntiProbeMissile(String antiProbeMissileName, String targetOwnerName, String targetProbeName) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canFireAntiProbeMissile(getLogin(), antiProbeMissileName, targetOwnerName, targetProbeName);
		}

		@Override
		public void fireAntiProbeMissile(String antiProbeMissileName, String targetOwnerName, String targetProbeName) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addFireAntiProbeMissileCommand(antiProbeMissileName, targetOwnerName, targetProbeName);
		}

		@Override
		public CommandCheckResult canLaunchProbe(String probeName, RealLocation destination) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canLaunchProbe(getLogin(), probeName, destination);
		}

		@Override
		public void launchProbe(String probeName, RealLocation destination) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addLaunchProbeCommand(probeName, destination);
		}

		@Override
		public CommandCheckResult canMakeStarships(String planetName, Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMake) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canMakeStarships(getLogin(), planetName, starshipsToMake);
		}

		@Override
		public void makeStarships(String planetName, Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMake) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addMakeStarshipsCommand(planetName, starshipsToMake);
		}

		@Override
		public CommandCheckResult canMakeProbes(String planetName, String probeName, int quantity) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canMakeProbes(getLogin(), planetName, probeName, quantity);
		}

		@Override
		public void makeProbes(String planetName, String probeName, int quantity) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addMakeProbesCommand(planetName, probeName, quantity);
		}

		@Override
		public CommandCheckResult canMakeAntiProbeMissiles(String planetName, String antiProbeMissileName, int quantity) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canMakeAntiProbeMissiles(getLogin(), planetName, antiProbeMissileName, quantity);
		}

		@Override
		public void makeAntiProbeMissiles(String planetName, String antiProbeMissileName, int quantity) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addMakeAntiProbeMissilesCommand(planetName, antiProbeMissileName, quantity);
		}

		@Override
		public CommandCheckResult canModifyCarbonOrder(String originCelestialBodyName, Stack<CarbonOrder> nextCarbonOrders) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canModifyCarbonOrder(getLogin(), originCelestialBodyName, nextCarbonOrders);
		}

		@Override
		public void modifyCarbonOrder(String originCelestialBodyName, Stack<CarbonOrder> nextCarbonOrders) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addModifyCarbonOrderCommand(originCelestialBodyName, nextCarbonOrders);
		}

		@Override
		public CommandCheckResult canMoveFleet() throws RpcException, StateMachineNotExpectedEventException
		{
			// if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			// TODO Auto-generated method stub
			throw new NotImplementedException();
		}

		@Override
		public void moveFleet(String fleetName, Stack<org.axan.sep.common.Fleet.Move> checkpoints) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addMoveFleetCommand(fleetName, checkpoints);
		}

		@Override
		public CommandCheckResult canSettleGovernment(String planetName) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canSettleGovernment(getLogin(), planetName);
		}

		@Override
		public void settleGovernment(String planetName) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
		{
			getGameMove().addSettleGovernmentCommand(planetName);
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
		public CommandCheckResult canEndTurn() throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return new CommandCheckResult();
		}

		@Override
		public CommandCheckResult canResetTurn() throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return new CommandCheckResult();
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
		SEPPausedGame(SEPServer server, ServerUser user)
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
		gameServer = new GameServer(gameServerListener, port, timeOut);
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
		log.log(Level.INFO, "Resolving new turn");
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
					log.log(Level.WARNING, "RpcException(" + name + ") : " + e.getMessage());
				}
			}
		}
	}

	/**
	 * @return
	 */
	public Set<Player> getPlayerList()
	{
		Set<Player> result = new TreeSet<Player>();

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
		log.log(Level.INFO, "Server started");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.eplib.clientserver.IServer#stop()
	 */
	@Override
	public void stop()
	{
		log.log(Level.INFO, "Stopping server");
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
		log.log(Level.INFO, "Terminating server");
		gameServer.terminate();
		threadPool.shutdown();
	}

	public String getServerAdminKey()
	{
		return gameServer.getServerAdminKey();
	}

	private final GameServerListener	gameServerListener	= new GameServerListener()
															{
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
															};

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

}
