package org.axan.sep.server.ai;

import java.util.List;
import java.util.Vector;

import org.axan.sep.common.AbstractGameCommandCheck;
import org.axan.sep.common.IGame;
import org.axan.sep.common.IGameCommand;
import org.axan.sep.common.LocalGame;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.IGameCommand.GameCommandException;

public class UncheckedLocalGame implements IGame
{
	private final LocalGame.Client client;
	private final PlayerGameBoard initialGameBoard;
	private final List<IGameCommand> commands;
	
	public UncheckedLocalGame(LocalGame.Client client, PlayerGameBoard initialGameBoard)
	{
		this.client = client;
		this.initialGameBoard = initialGameBoard;
		this.commands = new Vector<IGameCommand>();
	}

	@Override
	public AbstractGameCommandCheck canEndTurn()
	{
		return new AbstractGameCommandCheck(null) {};
	}

	@Override
	public AbstractGameCommandCheck canRedo()
	{
		return new AbstractGameCommandCheck(null) {};
	}

	@Override
	public AbstractGameCommandCheck canResetTurn()
	{
		return new AbstractGameCommandCheck(null) {};
	}

	@Override
	public AbstractGameCommandCheck canUndo()
	{
		return new AbstractGameCommandCheck(null) {};
	}

	@Override
	public void endTurn() throws GameCommandException
	{
		try
		{
			client.endTurn(getCommands());
		}
		catch(Throwable t)
		{
			throw new GameCommandException(t);
		}
	}

	@Override
	public void executeCommand(IGameCommand command) throws GameCommandException
	{
		commands.add(command);
	}

	@Override
	public List<IGameCommand> getCommands()
	{
		return commands;
	}

	@Override
	public PlayerGameBoard getGameBoard()
	{
		return initialGameBoard;
	}

	@Override
	public void redo() throws GameCommandException
	{
		throw new GameCommandException("Not implemented in unchecked local game version.");
	}

	@Override
	public void resetTurn() throws GameCommandException
	{
		throw new GameCommandException("Not implemented in unchecked local game version.");
	}

	@Override
	public void undo() throws GameCommandException
	{
		commands.remove(commands.size()-1);
	}
	
	public void undo(IGameCommand command)
	{
		commands.remove(command);
	}

}
