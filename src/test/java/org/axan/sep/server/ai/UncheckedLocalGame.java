package org.axan.sep.server.ai;

import java.util.List;
import java.util.Vector;

import org.axan.sep.common.AbstractGameCommandCheck;
import org.axan.sep.common.GameCommand;
import org.axan.sep.common.IGame;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.IGameBoard.GameBoardException;

public class UncheckedLocalGame implements IGame
{
	private final IGame.Client client;
	private final PlayerGameBoard initialGameBoard;
	private final List<GameCommand<?>> commands;
	
	public UncheckedLocalGame(IGame.Client client, PlayerGameBoard initialGameBoard)
	{
		this.client = client;
		this.initialGameBoard = initialGameBoard;
		this.commands = new Vector<GameCommand<?>>();
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
	public void endTurn() throws GameBoardException
	{
		try
		{
			client.endTurn(getCommands());
		}
		catch(Throwable t)
		{
			throw new GameBoardException(t);
		}
	}

	@Override
	public void executeCommand(GameCommand<?> command) throws GameBoardException
	{
		commands.add(command);
	}

	@Override
	public List<GameCommand<?>> getCommands()
	{
		return commands;
	}

	@Override
	public PlayerGameBoard getGameBoard()
	{
		return initialGameBoard;
	}

	@Override
	public void redo() throws GameBoardException
	{
		throw new GameBoardException("Not implemented in unchecked local game version.");
	}

	@Override
	public void resetTurn() throws GameBoardException
	{
		throw new GameBoardException("Not implemented in unchecked local game version.");
	}

	@Override
	public void undo()
	{
		commands.remove(commands.size()-1);
	}
	
	public void undo(GameCommand<?> command)
	{
		commands.remove(command);
	}

}
