package org.axan.sep.common;

import java.util.Map;

import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;

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
	
	Map<IPlayer, IPlayerConfig> getPlayerList() throws GameBoardException;
	
	///////////////////// Commands
	
	//IGameBoard build(String playerLogin, String celestialBodyName, eBuildingType buildingType) throws GameBoardException;
	
}