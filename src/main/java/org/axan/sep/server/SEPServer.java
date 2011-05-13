/**
 * @author Escallier Pierre
 * @file SEPServer.java
 * @date 23 juin 08
 */
package org.axan.sep.server;

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
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.IServer;
import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.eplib.gameserver.server.GameServer;
import org.axan.eplib.gameserver.server.GameServer.ExecutorFactory;
import org.axan.eplib.gameserver.server.GameServer.GameServerListener;
import org.axan.eplib.gameserver.server.GameServer.ServerUser;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.sep.common.CommandCheckResult;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.Player;
import org.axan.sep.common.PlayerConfig;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.GameConfigCopier.GameConfigCopierException;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.GameCommand;
import org.axan.sep.common.Protocol.ServerGameCreation;
import org.axan.sep.common.Protocol.ServerPausedGame;
import org.axan.sep.common.Protocol.ServerRunningGame;
import org.axan.sep.common.Protocol.ServerRunningGame.RunningGameCommandException;
import org.axan.sep.common.Protocol.SEPImplementationException;

/**
 * SEPServer
 * server package main class.
 */
class SEPServer implements IServer
{

	public static Logger		log	= Logger.getLogger(SEPServer.class.getCanonicalName());

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

		/*
		 * (non-Javadoc)
		 * 
		 * @see common.Protocol.ServerRunningGame#getPlayerGameBoard()
		 */
		@Override
		public PlayerGameBoard getPlayerGameBoard() throws RpcException, StateMachineNotExpectedEventException
		{
			try
			{
				// TODO: Be able to compute a full player game board (including previous turn) from the current server game board.
				return sepServer.getCurrentGame().getPlayerGameBoard(user.getLogin());
			}
			catch(GameBoardException e)
			{
				log.log(Level.SEVERE, "Player game board transformation error.", e);
				this.getServerPlayer().abort(e);
				return null;
			}
		}

		@Override
		public CommandCheckResult canSendMessage(String msg) throws RpcException, StateMachineNotExpectedEventException
		{
			// TODO
			throw new SEPImplementationException("Not Implemented");
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
		public void endTurn(List<GameCommand<?>> commands) throws RpcException ,StateMachineNotExpectedEventException, SEPImplementationException, RunningGameCommandException
		{
			sepServer.getCurrentGame().endTurn(getLogin(), commands);
			
			sepServer.threadPool.execute(new Runnable()
			{

				@Override
				public void run()
				{
					sepServer.checkForNextTurn();
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
		log.log(Level.FINEST, "Checking for next turn...");

		synchronized(players)
		{
			for(String name : players.keySet())
			{
				if (!getCurrentGame().isTurnEnded(name)) return;
			}
		}

		log.log(Level.INFO, "Resolving new turn");
		try
		{
			getCurrentGame().resolveCurrentTurn();
		}
		catch(RunningGameCommandException e)
		{
			log.log(Level.SEVERE, "Running game command error.", e);
			terminate();
			return;
		}

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
					players.get(name).getClientInterface().receiveNewTurnGameBoard(getCurrentGame().getPlayerGameBoard(name));
				}
				catch(RpcException e)
				{
					log.log(Level.WARNING, "RpcException(" + name + ") : " + e.getMessage());
				}
				catch(GameBoardException e)
				{
					log.log(Level.SEVERE, "Player game board error.", e);
					terminate();
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
	
	private void initCurrentGame() throws GameBoardException
	{
		synchronized(this)
		{
			if (game == null)
			{
				try
				{
					game = new ServerGame(new SQLiteGameBoard(getPlayerList(), this.gameConfig));
				}
				catch(Throwable t)
				{
					throw new GameBoardException(t);
				}
			}
		}
	}

	private ServerGame getCurrentGame()
	{
		while(game == null)
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
						server.initCurrentGame();
					}
					catch(GameBoardException e)
					{
						log.log(Level.SEVERE, "GameBoard creation error.", e);
						server.terminate();
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
				server.game = ServerGame.load(ois);
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
