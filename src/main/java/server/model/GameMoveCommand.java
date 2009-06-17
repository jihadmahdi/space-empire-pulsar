package server.model;

import common.PlayerGameBoard;

/**
 * Represent a game move command (ie: build, embark, ...).
 */
public abstract class GameMoveCommand
{
	protected String playerLogin;
	
	public GameMoveCommand(String playerLogin)
	{
		this.playerLogin = playerLogin;
	}

	abstract protected GameBoard apply(GameBoard originalGameBoard);
}
