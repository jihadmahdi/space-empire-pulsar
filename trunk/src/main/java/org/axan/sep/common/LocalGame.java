package org.axan.sep.common;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import org.axan.sep.common.IGameBoard.GameBoardException;

/**
 * This class represent the game for a client from local point of view.
 * It makes the client able to execute command and see a preview of the game after each command.
 * Finally the client has to get back the command list to send it to the server and ends his turn.
 */
@Deprecated
public class LocalGame implements IGame
{
	/** Ordered map of each gameBoard view and the command that leads to the next gameBoard view. */ 
	private SortedMap<PlayerGameBoard, GameCommand<?>> commands = new TreeMap<PlayerGameBoard, GameCommand<?>>(new Comparator<PlayerGameBoard>()
			{
				public int compare(PlayerGameBoard o1, PlayerGameBoard o2)
				{
					return o1.getTurn()*1000+o1.getLocalTurn() - (o2.getTurn()*1000+o2.getLocalTurn());
				}
			});
	
	private Stack<GameCommand<?>> undoneCommands = new Stack<GameCommand<?>>();
	
	private final IGame.Client client;
	
	/**
	 * Must provide the initial gameBoard.
	 * @param initialGameBoard Initial gameBoard.
	 */
	public LocalGame(IGame.Client client, PlayerGameBoard initialGameBoard)
	{
		this.commands.put(initialGameBoard, null);
		this.client = client;
	}
	
	/**
	 * Add a command to the current gameBoard and execute it to preview the next gameBoard.
	 * @param command Command to execute.
	 * @throws LocalGameCommandException If the command cannot be applied to the current gameBoard state. 
	 */
	@Override
	public synchronized void executeCommand(GameCommand<?> command) throws GameBoardException
	{
		reExecuteCommand(command);
		undoneCommands.clear();
		client.refreshLocalGameBoard(getGameBoard());
	}
	
	private synchronized void reExecuteCommand(GameCommand<?> command) throws GameBoardException
	{
		if (turnEnded)
		{
			throw new GameBoardException("Turn already ended.");
		}
		
		PlayerGameBoard current = commands.lastKey();
		if (commands.get(current) != null) throw new RuntimeException("Last gameBoard is not expected to have a command.");

		PlayerGameBoard next = PlayerGameBoard.class.cast(command.apply(current));
		next.incLocalTurn();
		
		commands.put(current, command);
		commands.put(next, null);
	}
	
	@Override
	public synchronized AbstractGameCommandCheck canResetTurn()
	{
		AbstractGameCommandCheck check = new AbstractGameCommandCheck(null){};
		
		if (turnEnded)
		{
			check.setImpossibilityReason("Turn already ended.");
			return check;
		}
		
		return check;
	}
	
	/**
	 * Clear all commands and reset to initial gameBoard.
	 */
	@Override
	public synchronized void resetTurn() throws GameBoardException
	{
		AbstractGameCommandCheck check = canResetTurn();
		if (!check.isPossible())
		{
			throw new GameBoardException(check.getReason());
		}
		
		PlayerGameBoard initialGameBoard = commands.firstKey();
		commands.clear();
		commands.put(initialGameBoard, null);
		
		client.refreshLocalGameBoard(getGameBoard());
	}
	
	@Override
	public synchronized AbstractGameCommandCheck canUndo()
	{
		AbstractGameCommandCheck check = new AbstractGameCommandCheck(null){};
		
		if (turnEnded)
		{
			check.setImpossibilityReason("Turn already ended.");
			return check;
		}
		
		if (commands.size() <= 1)
		{
			check.setImpossibilityReason("Command list is empty.");
			return check;
		}
		
		return check;
	}
	
	@Override
	public synchronized void undo() throws GameBoardException
	{
		AbstractGameCommandCheck check = canUndo();
		if (!check.isPossible())
		{
			throw new GameBoardException(check.getReason());
		}
		
		commands.remove(commands.lastKey());		
		undoneCommands.push(commands.get(commands.lastKey()));
		commands.put(commands.lastKey(), null);
		
		client.refreshLocalGameBoard(getGameBoard());
	}

	@Override
	public synchronized AbstractGameCommandCheck canRedo()
	{
		AbstractGameCommandCheck check = new AbstractGameCommandCheck(null){};
		
		if (turnEnded)
		{
			check.setImpossibilityReason("Turn already ended.");
			return check;
		}
		
		if (undoneCommands.isEmpty())
		{
			check.setImpossibilityReason("Empty undone command list.");
			return check;
		}
		
		return check;
	}

	@Override
	public synchronized void redo() throws GameBoardException
	{
		AbstractGameCommandCheck check = canRedo();
		if (!check.isPossible())
		{
			throw new GameBoardException(check.getReason());
		}
		
		reExecuteCommand(undoneCommands.pop());
		
		client.refreshLocalGameBoard(getGameBoard());
	}
	
	/**
	 * Get last gameBoard state (all known commands applied).
	 * @return
	 */
	@Override
	public PlayerGameBoard getGameBoard()
	{
		return commands.lastKey();
	}
	
	/**
	 * Return a list of all applied commands.
	 * @return
	 */
	@Override
	public List<GameCommand<?>> getCommands()
	{
		return new LinkedList<GameCommand<?>>(commands.values());		
	}
	
	private Boolean turnEnded = false;	

	@Override
	public synchronized AbstractGameCommandCheck canEndTurn()
	{
		synchronized(turnEnded)
		{
			AbstractGameCommandCheck check = new AbstractGameCommandCheck(null) {};
			
			if (turnEnded)
			{
				check.setImpossibilityReason("Turn already ended.");
				return check;
			}
			
			return check;
		}
	}

	@Override
	public synchronized void endTurn() throws GameBoardException
	{
		AbstractGameCommandCheck check = canEndTurn();
		if (!check.isPossible())
		{
			throw new GameBoardException(check.toString());
		}
		
		try
		{
			client.endTurn(getCommands());
		}
		catch(Throwable t)
		{
			throw new GameBoardException(t);
		}
		
		turnEnded = true;
		
		client.refreshLocalGameBoard(getGameBoard());
	}
		
}
