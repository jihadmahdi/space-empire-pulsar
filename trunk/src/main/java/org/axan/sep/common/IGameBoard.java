package org.axan.sep.common;

import java.util.Set;

import org.axan.sep.common.Protocol.eBuildingType;

public interface IGameBoard
{
	
	public static class GameBoardException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		public GameBoardException(String message)
		{
			super(message);
		}
		
		public GameBoardException(Throwable t)
		{
			super(t);
		}
		
		public GameBoardException(String message, Throwable t)
		{
			super(message, t);
		}
	}
	
	/**
	 * Return current gameboard players list.
	 */
	Set<Player> getPlayers() throws GameBoardException;
	
	///////////////////// Commands
	
	IGameBoard build(String playerLogin, String celestialBodyName, eBuildingType buildingType) throws GameBoardException;
	
}