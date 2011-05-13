/**
 * @author Escallier Pierre
 * @file ServerGame.java
 * @date 1 juin 2009
 */
package org.axan.sep.server;

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
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.axan.sep.common.CommandCheckResult;
import org.axan.sep.common.GameConfig;
import org.axan.sep.common.GameCommand;
import org.axan.sep.common.Player;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.GameConfigCopier.GameConfigCopierException;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.Protocol.ServerRunningGame.RunningGameCommandException;
import org.axan.sep.common.Protocol.SEPImplementationException;
import org.axan.sep.server.SEPServer;

/**
 * Represent a running game at a specific turn. It also provide previous turns
 * archives.
 */
class ServerGame implements Serializable
{
	private static final long							serialVersionUID	= 1L;

	private static final Logger							log					= SEPServer.log;

	/** Resolved game turns. */
	private GameBoard						currentGameBoard			= null;

	private transient Map<String, List<GameCommand<?>>>	playersCurrentMove = new HashMap<String, List<GameCommand<?>>>();

	public ServerGame(GameBoard initialGameBoard) throws GameBoardException
	{
		this.currentGameBoard = initialGameBoard;
				
		for(Player p : this.currentGameBoard.getPlayers())
		{
			playersCurrentMove.put(p.getName(), null);
		}
	}
	
	public PlayerGameBoard getPlayerGameBoard(String playerLogin) throws GameBoardException
	{
		return currentGameBoard.getPlayerGameBoard(playerLogin);
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
	
	public boolean isTurnEnded(String playerLogin)
	{
		// TODO: Implement the case for which a player who already loss the game don't need to end turn (actually don't even play anymore).
		// TODO: + any case for which the player just has to pass his turn (pulsar...).
		return playersCurrentMove.get(playerLogin) != null;
	}

	public void resolveCurrentTurn() throws RunningGameCommandException
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
