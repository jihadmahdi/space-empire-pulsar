package org.axan.sep.common.db.sqlite;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import org.axan.sep.common.AbstractGameCommandCheck;
import org.axan.sep.common.IGame;
import org.axan.sep.common.IGameCommand;
import org.axan.sep.common.IGameCommand.GameCommandException;
import org.axan.sep.common.db.sqlite.SEPClientSQLiteDB;

/**
 * This class represent the game for a client from local point of view.
 * It makes the client able to execute command and see a preview of the game after each command.
 * Finally the client has to get back the command list to send it to the server and ends his turn.
 */
public class SQLiteLocalGame implements IGame<SEPClientSQLiteDB>
{
	/** Ordered map of each gameBoard view and the command that leads to the next gameBoard view. */ 
	private SortedMap<SEPClientSQLiteDB, IGameCommand<SEPClientSQLiteDB>> commands = new TreeMap<SEPClientSQLiteDB, IGameCommand<SEPClientSQLiteDB>>(new Comparator<SEPClientSQLiteDB>()
			{
				public int compare(SEPClientSQLiteDB o1, SEPClientSQLiteDB o2)
				{
					return o1.getTurn()*1000+o1.getLocalTurn() - (o2.getTurn()*1000+o2.getLocalTurn());
				}
			});
	
	private Stack<IGameCommand<SEPClientSQLiteDB>> undoneCommands = new Stack<IGameCommand<SEPClientSQLiteDB>>();
	
	private final IGame.Client<SEPClientSQLiteDB> client;
	
	/**
	 * Must provide the initial gameBoard.
	 * @param initialGameBoard Initial gameBoard.
	 */
	public SQLiteLocalGame(IGame.Client<SEPClientSQLiteDB> client, SEPClientSQLiteDB initialGameBoard)
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
	public synchronized void executeCommand(IGameCommand<SEPClientSQLiteDB> command) throws GameCommandException
	{
		reExecuteCommand(command);
		undoneCommands.clear();
		client.refreshLocalGameBoard(getGameBoard());
	}
	
	private synchronized void reExecuteCommand(IGameCommand<SEPClientSQLiteDB> command) throws GameCommandException
	{
		if (turnEnded)
		{
			throw new GameCommandException("Turn already ended.");
		}
		
		SEPClientSQLiteDB current = commands.lastKey();
		if (commands.get(current) != null) throw new RuntimeException("Last gameBoard is not expected to have a command.");

		SEPClientSQLiteDB next = SEPClientSQLiteDB.class.cast(command.apply(current));
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
	public synchronized void resetTurn() throws GameCommandException
	{
		AbstractGameCommandCheck check = canResetTurn();
		if (!check.isPossible())
		{
			throw new GameCommandException(check.getReason());
		}
		
		SEPClientSQLiteDB initialGameBoard = commands.firstKey();
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
	public synchronized void undo() throws GameCommandException
	{
		AbstractGameCommandCheck check = canUndo();
		if (!check.isPossible())
		{
			throw new GameCommandException(check.getReason());
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
	public synchronized void redo() throws GameCommandException
	{
		AbstractGameCommandCheck check = canRedo();
		if (!check.isPossible())
		{
			throw new GameCommandException(check.getReason());
		}
		
		reExecuteCommand(undoneCommands.pop());
		
		client.refreshLocalGameBoard(getGameBoard());
	}
	
	/**
	 * Get last gameBoard state (all known commands applied).
	 * @return
	 */
	@Override
	public SEPClientSQLiteDB getGameBoard()
	{
		return commands.lastKey();
	}
	
	/**
	 * Return a list of all applied commands.
	 * @return
	 */
	@Override
	public List<IGameCommand<SEPClientSQLiteDB>> getCommands()
	{
		return new LinkedList<IGameCommand<SEPClientSQLiteDB>>(commands.values());		
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
	public synchronized void endTurn() throws GameCommandException
	{
		AbstractGameCommandCheck check = canEndTurn();
		if (!check.isPossible())
		{
			throw new GameCommandException(check.toString());
		}
		
		try
		{
			client.endTurn(getCommands());
		}
		catch(Throwable t)
		{
			throw new GameCommandException(t);
		}
		
		turnEnded = true;
		
		client.refreshLocalGameBoard(getGameBoard());
	}
		
}
