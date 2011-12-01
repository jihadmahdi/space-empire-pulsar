package org.axan.sep.common;

import java.util.Map;
import java.util.Set;

import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.orm.Player;
import org.axan.sep.common.db.orm.PlayerConfig;

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
	
	Map<Player, PlayerConfig> getPlayerList() throws GameBoardException;
	
	///////////////////// Commands
	
	IGameBoard build(String playerLogin, String celestialBodyName, eBuildingType buildingType) throws GameBoardException;
	
}