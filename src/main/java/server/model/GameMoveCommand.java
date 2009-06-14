package server.model;

import common.PlayerGameBoard;

/**
 * Represent a game move command (ie: build, embark, ...).
 */
public abstract class GameMoveCommand
{
	private final GameBoard originalGameBoard;
	protected GameBoard finalGameBoard;
	
	public GameMoveCommand(GameBoard originalGameBoard)
	{
		this.originalGameBoard = originalGameBoard;
	}

	public PlayerGameBoard getPlayerGameBoard(String playerLogin)
	{
		return getFinalGameBoard().getPlayerGameBoard(playerLogin);
	}
	
	public GameBoard getFinalGameBoard()
	{
		if (finalGameBoard == null)
		{
			finalGameBoard = apply();
		}
		return finalGameBoard;
	}
	
	abstract protected GameBoard apply();
}
