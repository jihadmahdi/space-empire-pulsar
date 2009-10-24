package org.axan.sep.server.model;

import org.axan.sep.common.PlayerGameBoard;

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
