/**
 * @author Escallier Pierre
 * @file ServerGame.java
 * @date 1 juin 2009
 */
package org.axan.sep.server;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.eplib.clientserver.rpc.RpcException;
import org.axan.eplib.gameserver.common.IServerUser.ServerPrivilegeException;
import org.axan.eplib.orm.ISQLDataBase;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.eplib.statemachine.StateMachine.StateMachineNotExpectedEventException;
import org.axan.eplib.utils.Basic;
import org.axan.sep.common.CommandCheckResult;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.GameCommand;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.Protocol;
import org.axan.sep.common.GameConfigCopier.GameConfigCopierException;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.Protocol.ServerRunningGame.RunningGameCommandException;
import org.axan.sep.common.Protocol.SEPImplementationException;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.orm.Player;
import org.axan.sep.common.db.orm.PlayerConfig;
import org.axan.sep.server.SEPServer;

/**
 * Represent a running game at a specific turn. It also provide previous turns
 * archives.
 */
class ServerGame implements Serializable
{
	private static final long							serialVersionUID	= 1L;

	private static final Logger							log					= SEPServer.log;
	
	private static final Random							rnd = new Random();

	/** Resolved game turns. */
	private GameBoard						currentGameBoard			= null;

	/** Current turn players moves. */
	private transient Map<String, List<GameCommand<?>>>	playersCurrentMove = new HashMap<String, List<GameCommand<?>>>();
	
	/** List players during game creation, must not be used whence game is ran. */
	private transient Map<Player, PlayerConfig> players = new TreeMap<Player, PlayerConfig>();
	
	/** During game creation, temporary game config. */
	private transient GameConfig gameConfig = new GameConfig();

	public ServerGame()
	{
		//TODO: Testing config, to remove later.
		gameConfig.setDimX(15);
		gameConfig.setDimY(15);
		gameConfig.setDimZ(3);
	}
	
	public void addPlayer(String playerLogin) throws GameBoardException
	{
		if (!isGameInCreation()) throw new GameBoardException("Cannot update game config when game is already running.");
		
		for(Player p : players.keySet())
		{
			if (p.getName().compareTo(playerLogin) == 0)
			{
				throw new GameBoardException("Player "+playerLogin+" already exists.");
			}
		}
		
		//TODO: Portrait & Symbol
		players.put(new Player(playerLogin), new PlayerConfig(playerLogin, Basic.colorToString(new Color(rnd.nextInt(0xFFFFFF))), null, null));		
	}
	
	public void removePlayer(String playerLogin) throws GameBoardException
	{
		if (!isGameInCreation()) throw new GameBoardException("Cannot update game config when game is already running.");
		
		for(Player p : players.keySet())
		{
			if (p.getName().compareTo(playerLogin) == 0)
			{
				players.remove(p);
				return;
			}
		}
		
		throw new GameBoardException("Player "+playerLogin+" is unknown.");
	}
	
	public Player getPlayer(String playerLogin) throws GameBoardException
	{
		if (isGameInCreation()) // Game in creation
		{
			for(Player p : players.keySet())
			{
				if (p.getName().compareTo(playerLogin) == 0) return p;
			}
			
			return null;
		}
		else
		{
			return currentGameBoard.getPlayer(playerLogin);
		}
	}
	
	public Map<Player, PlayerConfig> getPlayerList() throws GameBoardException
	{
		if (isGameInCreation()) // Game in creation
		{
			return players;
		}
		else
		{
			return currentGameBoard.getPlayerList();			
		}
	}
	
	public IGameConfig getConfig()
	{
		if (isGameInCreation())
		{
			return gameConfig;
		}
		else
		{
			return currentGameBoard.getConfig();
		}
	}
	
	public void updateGameConfig(GameConfig gameCfg) throws GameBoardException
	{
		if (!isGameInCreation()) throw new GameBoardException("Cannot update game config when game is already running.");		
		this.gameConfig = gameCfg;		
	}
	
	public void updatePlayerConfig(PlayerConfig playerCfg) throws GameBoardException
	{
		if (!isGameInCreation()) throw new GameBoardException("Cannot update game config when game is already running.");
		
		for(Player p : players.keySet())
		{
			if (p.getName().compareTo(playerCfg.getName()) == 0)
			{
				players.put(p, playerCfg);
				return;
			}
		}
		
		throw new GameBoardException("Player "+playerCfg.getName()+" unknown.");		
	}
	
	public boolean isGameInCreation()
	{
		return currentGameBoard == null;
	}
	
	public void run(ISQLDataBase db) throws GameBoardException
	{
		for(Player p : players.keySet())
		{
			playersCurrentMove.put(p.getName(), null);
		}
		
		try
		{
			currentGameBoard = new GameBoard(db, players, gameConfig);
		}
		catch(Exception e)
		{
			throw new GameBoardException(e);
		}
	}
	
	public PlayerGameBoard getPlayerGameBoard(ISQLDataBase db, String playerLogin) throws GameBoardException
	{
		return currentGameBoard.getPlayerGameBoard(db, playerLogin);
	}
	
	private void checkCommandResult(CommandCheckResult result) throws RunningGameCommandException
	{
		if (!result.isPossible())
		{
			throw new RunningGameCommandException(result.getReason());
		}
	}
	
	/*
	private void executeCommand(GameCommand<?> command) throws RunningGameCommandException
	{		
		Method method;
		CommandCheckResult result;
		try
		{
			String methodName = "can"+command.getClass().getSimpleName();
			if (methodName.endsWith("Command")) methodName = methodName.replaceAll("Command", "");
			method = GameBoard.class.getMethod(methodName, String.class, command.getParams().getClass());			
			result = (CommandCheckResult) method.invoke(currentGameBoard, command.playerLogin, command.getParams());
		}
		catch(Throwable t)
		{
			throw new RunningGameCommandException(t);
		}
		
		checkCommandResult(result);
		
		command.apply(currentGameBoard);
	}
	*/
	
	public void endTurn(String playerLogin, List<GameCommand<?>> commands) throws RunningGameCommandException
	{
		for(GameCommand<?> command : commands)
		{
			if (command == null) continue;
			try
			{
				command.apply(currentGameBoard);
			}
			catch(Throwable t)
			{				
				// TODO: Corrupted game, cannot recover from corrupted gameboard if crashed on Command.apply(GameBoard).
				
				if (RunningGameCommandException.class.isInstance(t))
				{
					throw RunningGameCommandException.class.cast(t);
				}
				else
				{
					throw new RunningGameCommandException(t);
				}
			}
			
			/*
			GameMoveCommand<?> gameMoveCommand;
			
			String serverClassName = GameMoveCommand.class.getCanonicalName()+"$"+command.getClass().getSimpleName()+"Command";
			try
			{
				Class<? extends GameMoveCommand<?>> serverCommandClass = (Class<? extends GameMoveCommand<Object>>) Class.forName(serverClassName);
				Constructor<? extends GameMoveCommand<?>> serverCommandClassConstructor;
				serverCommandClassConstructor = serverCommandClass.getConstructor(String.class, command.getParams().getClass());
				gameMoveCommand = serverCommandClassConstructor.newInstance(playerLogin, command.getParams());
			}
			catch(Throwable t)
			{
				throw new SEPImplementationException("Client command '"+command.getClass().getSimpleName()+"' cannot match server expected class '"+serverClassName+"'", t);
			}
			
			try
			{
				executeCommand(gameMoveCommand);
			}
			catch(Throwable t)
			{				
				// TODO: Corrupted game, cannot recover from corrupted gameboard if crashed on Command.apply(GameBoard).
				
				if (RunningGameCommandException.class.isInstance(t))
				{
					throw RunningGameCommandException.class.cast(t);
				}
				else
				{
					throw new RunningGameCommandException(t);
				}
			}
			*/
		}
		
		playersCurrentMove.put(playerLogin, commands);
	}
	
	private boolean isTurnEnded(String playerLogin)
	{
		// TODO: Implement the case for which a player who already loss the game don't need to end turn (actually don't even play anymore).
		// TODO: + any case for which the player just has to pass his turn (pulsar...).
		return playersCurrentMove.get(playerLogin) != null;
	}
	
	public void checkForNextTurn() throws RunningGameCommandException
	{
		log.log(Level.FINEST, "Checking for next turn...");

		for(String playerLogin : playersCurrentMove.keySet())
		{
			if (!isTurnEnded(playerLogin)) return;
		}

		log.log(Level.INFO, "Resolving new turn");
		resolveCurrentTurn();
	}

	private void resolveCurrentTurn() throws RunningGameCommandException
	{		
		// Check all players have ended turn.
		for(String playerLogin : playersCurrentMove.keySet())
		{
			if (!isTurnEnded(playerLogin)) return;			
		}

		// Resolve the turn.
		currentGameBoard.resolveCurrentTurn();

		// Empty current turn command lists.
		for(String playerLogin : playersCurrentMove.keySet())
		{
			playersCurrentMove.put(playerLogin, null);
		}
	}

	public void save(ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(this);
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();

		out.writeInt(playersCurrentMove == null ? 0 : playersCurrentMove.keySet().size());
		if (playersCurrentMove != null) for(String playerName : playersCurrentMove.keySet())
		{
			out.writeObject(playerName);
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		if (playersCurrentMove == null) {playersCurrentMove = new HashMap<String, List<GameCommand<?>>>();}
		playersCurrentMove.clear();

		int nbPlayers = in.readInt();
		for(int i = 0; i < nbPlayers; ++i)
		{
			String playerName = String.class.cast(in.readObject());
			playersCurrentMove.put(playerName, null);
		}
	}

	private void readObjectNoData() throws ObjectStreamException
	{

	}

	public static ServerGame load(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{	
		return ServerGame.class.cast(ois.readObject());
	}
}
