package org.axan.sep.common;

import java.io.Serializable;

/**
 * Represent the game board for a specific player. It provide
 * informations about the universe since the creation.
 * Must be extended whith a DB specific implementation.
 */
public abstract class PlayerGameBoard implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	private int localTurn = 0;

	abstract public int getTurn();
	
	public int getLocalTurn()
	{
		return localTurn;
	}
	
	public void incLocalTurn()
	{
		++localTurn;
	}

}
