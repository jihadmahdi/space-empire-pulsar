/**
 * @author Escallier Pierre
 * @file SEPServer.java
 * @date 23 juin 08
 */
package org.axan.sep.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.ConnectionAbortedError;
import org.axan.eplib.clientserver.IServer;
import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.eplib.gameserver.server.GameServer;
import org.axan.eplib.gameserver.server.GameServer.ExecutorFactory;
import org.axan.eplib.gameserver.server.GameServer.GameServerListener;
import org.axan.eplib.gameserver.server.GameServer.ServerUser;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.GameConfigCopier;
import org.axan.sep.common.GameConfigCopier.GameConfigCopierException;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.Protocol.SEPImplementationError;
import org.axan.sep.common.Protocol.ServerGameCreation;
import org.axan.sep.common.Protocol.ServerPausedGame;
import org.axan.sep.common.Protocol.ServerRunningGame;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.db.ICommand;
import org.axan.sep.common.db.ICommand.GameCommandException;
import org.axan.sep.common.db.IDBFactory;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IGameEvent;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

// TODO: add synchronized to SEPServer and ServerGame methods that need it.

/**
 * SEPServer
 * server package main class.
 */
public class SEPServer implements IServer, IDBFactory
{

	public static Logger		log	= Logger.getLogger(SEPServer.class.getCanonicalName());
	private static Random		rnd = new Random();

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
		SEPCommon(SEPServer server, ServerUser user)
		{
			this.sepServer = server;
			this.user = user;
		}

		String getLogin()
		{
			return user.getLogin();
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
		public Map<IPlayer, IPlayerConfig> getPlayerList()
		{
			try
			{
				return sepServer.getGameInCreation().getPlayerList();
			}
			catch(GameBoardException e)
			{
				throw new ConnectionAbortedError(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.Protocol.ServerCommon#getGameConfig()
		 */
		@Override
		public IGameConfig getGameConfig()
		{
			return sepServer.getGameInCreation().getConfig();
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
					try
					{
						final IPlayer p = sepServer.getGameInCreation().getPlayer(user.getLogin());
						
						sepServer.doForEachConnectedPlayer(new DoItToOnePlayer()
						{

							@Override
							public void doIt(ServerPlayer player)
							{
								try
								{
									player.getClientInterface().receiveGameCreationMessage(p, msg);
								}
								catch(RpcException e)
								{
									log.log(Level.WARNING, "RpcException(" + player.getName() + ") : " + e.getMessage());
									player.abort(e);
								}
							}
						});
					}
					catch(GameBoardException e)
					{
						log.log(Level.SEVERE, "GameBoardException", e);
					}					
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

			synchronized(sepServer)
			{
				try
				{
					sepServer.getGameInCreation().updateGameConfig(gameCfg);
				}
				catch(GameBoardException e)
				{
					throw new ConnectionAbortedError(e);
				}
				
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
		public void updatePlayerConfig(final IPlayerConfig playerCfg)
		{
			try
			{
				sepServer.getGameInCreation().updatePlayerConfig(user.getLogin(), playerCfg);
			}
			catch(GameBoardException e)
			{
				throw new ConnectionAbortedError(e);
			}
			
			sepServer.threadPool.execute(new Runnable()
			{

				@Override
				public void run()
				{
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.Protocol.ServerRunningGame#getEntireGameLog()
		 */
		@Override
		public List<IGameEvent> getEntireGameLog() throws RpcException ,StateMachineNotExpectedEventException
		{
			try
			{
				return sepServer.getRunningGame().getEntireGameLog(user.getLogin());
			}
			catch(GameBoardException e)
			{
				// StateMachineNotExcpectedEventException should have been thrown before to reach that code. Or it means that it is a server-side call.
				throw new SEPImplementationError("Could not get entire game log for player '"+user.getLogin()+"'", e);
			}
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
					try
					{
						final IPlayer p = sepServer.getRunningGame().getPlayer(user.getLogin());
						
						sepServer.doForEachConnectedPlayer(new DoItToOnePlayer()
						{
	
							@Override
							public void doIt(ServerPlayer player)
							{								
								try
								{
									player.getClientInterface().receiveRunningGameMessage(p, msg);
								}
								catch(RpcException e)
								{
									log.log(Level.WARNING, "RpcException(" + player.getName() + ") : " + e.getMessage());
									player.abort(e);
								}							
							}
						});
					}
					catch(GameBoardException e)
					{
						log.log(Level.SEVERE, "GameBoardException", e);
					}
				}
			});
		}

		/*
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
		public CommandCheckResult canFormFleet(String planetName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<String> fleetToFormSpecialUnits) throws RpcException, StateMachineNotExpectedEventException
		{
			if (getGameMove().isTurnEnded()) return new CommandCheckResult("Turn already ended.");
			return getGameBoard().canFormFleet(getLogin(), planetName, fleetName, fleetToFormStarships, fleetToFormSpecialUnits);
		}

		@Override
		public void formFleet(String planetName, String fleetName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToFormStarships, Set<String> fleetToFormSpecialUnits) throws RpcException, StateMachineNotExpectedEventException, RunningGameCommandException
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
		*/

		@Override
		public void endTurn(List<ICommand> commands) throws RpcException, StateMachineNotExpectedEventException, GameCommandException
		{
			try
			{
				if (sepServer.getRunningGame().endTurn(getLogin(), commands))
				{
					// All players are ready for next turn.
					
					sepServer.threadPool.execute(new Runnable()
					{
						
						@Override
						public void run()
						{
							try
							{
								sepServer.getRunningGame().resolveNextTurn();
								sepServer.announceNewTurn();
							}
							catch(GameBoardException e)
							{
								log.log(Level.SEVERE, "Error on new game turn", e);
								sepServer.terminate();
							}
						}
					});
				}
			}
			catch(GameBoardException e)
			{
				throw new ConnectionAbortedError(e);
			}
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
		public Map<IPlayer, Boolean> getPlayerStateList() throws RpcException, StateMachineNotExpectedEventException
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
					try
					{
						final IPlayer p = sepServer.getRunningGame().getPlayer(user.getLogin());
						
						sepServer.doForEachConnectedPlayer(new DoItToOnePlayer()
						{
	
							@Override
							public void doIt(ServerPlayer player)
							{								
								try
								{
									player.getClientInterface().receivePausedGameMessage(p, msg);
								}
								catch(RpcException e)
								{
									log.log(Level.WARNING, "RpcException(" + player.getName() + ") : " + e.getMessage());
									player.abort(e);
								}							
							}
						});
					}
					catch(GameBoardException e)
					{
						log.log(Level.SEVERE, "GameBoardException", e);
					}
				}
			});
		}
	}

	private final GameServer				gameServer;
	private final Map<String, ServerPlayer>	players		= new HashMap<String, ServerPlayer>();

	private GameBoard game;

	public SEPServer(int port, long timeOut)
	{		
		gameServer = new GameServer(gameServerListener, port, timeOut);		
		
		try
		{
			game = new GameBoard(this);
			
			gameServer.enableFTPServer("./game", port+1);
			
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
	
	@Override
	public GraphDatabaseService createDB()
	{
		try
		{
			File dbDirectory = File.createTempFile("sepS", "");
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

	private Map<IPlayer, Boolean> getPlayerStateList()
	{
		Map<IPlayer, Boolean> result = new HashMap<IPlayer, Boolean>();

		synchronized(players)
		{
			for(String name : players.keySet())
			{
				ServerPlayer player = players.get(name);
				
				if (player != null)
				{
					try
					{
						result.put(getGameInCreation().getPlayer(name), player.isConnected());
					}
					catch(GameBoardException e)
					{
						if (player.isConnected())
						{
							log.log(Level.SEVERE, "Connected player "+name+" is unknown.", e);
						}
					}
				}
			}
		}

		return result;
	}	

	private synchronized void announceNewTurn()
	{
		synchronized(players)
		{
			doForEachConnectedPlayer(new DoItToOnePlayer()
			{				
				@Override
				public void doIt(ServerPlayer player)
				{
					try
					{
						player.getClientInterface().receiveNewTurnGameBoard(getRunningGame().getLastTurnEvents(player.getName()));
					}
					catch(RpcException e)
					{
						log.log(Level.WARNING, "RpcException(" + player.getName() + ") : " + e.getMessage());
					}
					catch(GameBoardException e)
					{
						log.log(Level.SEVERE, "Player game board error.", e);
						terminate();
					}
				}
			});
		}
	}		
	
	private synchronized void runGame() throws GameBoardException
	{
		synchronized(game)
		{
			if (game.isGameInCreation())
			{
				game.createUniverse();
				
				threadPool.execute(new Runnable()
				{
					
					@Override
					public void run()
					{
						try
						{
							refreshGameConfig();
							getRunningGame().resolveNextTurn();
							announceNewTurn();
						}
						catch(GameBoardException e)
						{
							log.log(Level.SEVERE, "Error on game start", e);
							terminate();
						}
					}
				});
			}
		}
	}

	private GameBoard getGameInCreation()
	{
		return game;
	}
	
	private GameBoard getRunningGame()
	{
		while(game.isGameInCreation())
		{
			try
			{
				Thread.sleep(500);
			}
			catch(InterruptedException e)
			{
				throw new Error(e);
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
	 * @see org.axan.eplib.clientserver.IServer#getPort()
	 */	
	@Override
	public int getPort()
	{
		return gameServer.getPort();
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

	static class SEPGameServerListener implements GameServerListener
	{
		private final SEPServer server;
		
		public SEPGameServerListener(SEPServer server)
		{
			this.server = server;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.axan.eplib.gameserver.server.GameServer.GameServerListener#playerLoggedIn(org.axan.eplib.gameserver.server.GameServer.ServerUser)
		 */
		@Override
		public void playerLoggedIn(ServerUser player)
		{
			synchronized(server.players)
			{
				if (server.players.containsKey(player.getLogin()))
				{
					server.players.get(player.getLogin()).setServerUser(player);
				}
				else
				{
					server.players.put(player.getLogin(), new ServerPlayer(player));
					if (server.getGameInCreation().isGameInCreation())
					{
						try
						{
							server.getGameInCreation().addPlayer(player.getLogin());
						}
						catch(GameBoardException e)
						{
							log.log(Level.SEVERE, "addPlayer error", e);
							player.disconnect();
						}
					}
				}
			}
			server.refreshPlayerList();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.axan.eplib.gameserver.server.GameServer.GameServerListener#playerLoggedOut(org.axan.eplib.gameserver.server.GameServer.ServerUser)
		 */
		@Override
		public void playerLoggedOut(ServerUser player)
		{
			if (server.players.containsKey(player.getLogin()))
			{
				server.players.get(player.getLogin()).setServerUser(null);
				if (server.getGameInCreation().isGameInCreation())
				{
					try
					{
						server.getGameInCreation().removePlayer(player.getLogin());
					}
					catch(GameBoardException e) { /* NOOP */ }
				}
				server.refreshPlayerList();
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
			server.threadPool.execute(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						server.runGame();
					}
					catch(GameBoardException e)
					{
						e.printStackTrace();						
						server.terminate();
						log.log(Level.SEVERE, "GameBoard creation error.", e);
					}
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

		@Override
		public ObjectInputStream getGameLoadingStream(String savedGameId) throws FileNotFoundException, IOException
		{
			return new ObjectInputStream(new FileInputStream(SEPUtils.SAVE_SUBDIR+SEPUtils.getSaveFileName(savedGameId)));
		}

		@Override
		public ObjectOutputStream getGameSavingStream(String saveGameId) throws FileNotFoundException, IOException
		{
			return new ObjectOutputStream(new FileOutputStream(SEPUtils.SAVE_SUBDIR+SEPUtils.getSaveFileName(saveGameId)));			
		}

		@Override
		public void loadGame(ObjectInputStream ois) throws Throwable
		{
			synchronized(server)
			{
				server.game = GameBoard.load(ois);
			}
		}

		@Override
		public void saveGame(ObjectOutputStream oos) throws Throwable
		{
			synchronized(server)
			{
				server.game.save(oos);
			}			
		}
	}
	
	private final GameServerListener	gameServerListener	= new SEPGameServerListener(this);

	private void refreshGameConfig()
	{
		IGameConfig iConfig = getGameInCreation().getConfig();
		final GameConfig config;
		
		if (GameConfig.class.isInstance(iConfig))
		{
			config = (GameConfig) iConfig;
		}
		else
		{
			config = new GameConfig();
			try
			{
				GameConfigCopier.copy(IGameConfig.class, iConfig, config);
			}
			catch(GameConfigCopierException e)
			{
				log.log(Level.SEVERE, "Cannot copy from DB config to GameConfig object.", e);
				terminate();
			}
		}
		
		doForEachConnectedPlayer(new DoItToOnePlayer()
		{

			@Override
			public void doIt(ServerPlayer player)
			{
				try
				{					
					player.getClientInterface().refreshGameConfig(config);
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
		try
		{
			final Map<IPlayer, IPlayerConfig> playerList = getGameInCreation().getPlayerList();
			
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
		catch(GameBoardException e)
		{
			log.log(Level.SEVERE, "GameBoardException", e);
			return;
		}		
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
