package org.axan.sep.server.model;

import org.axan.sep.common.Protocol.ServerRunningGame.RunningGameCommandException;
import org.axan.sep.server.model.ISEPServerDataBase.SEPServerDataBaseException;


/**
 * Represent a game move command (ie: build, embark, ...).
 */
public abstract class GameMoveCommand<P>
{
	protected String playerLogin;
	protected final P params;
	
	public GameMoveCommand(String playerLogin, P params)
	{
		this.playerLogin = playerLogin;
		this.params = params;
	}
	
	public P getParams()
	{
		return params;
	}

	abstract protected GameBoard apply(GameBoard originalGameBoard) throws RunningGameCommandException;
}
