package org.axan.sep.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.utils.Basic;
import org.axan.sep.common.IGameBoard.GameBoardException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.Player;
import org.axan.sep.common.db.orm.PlayerConfig;

/**
 * Represent the game board for a specific player. It provide
 * informations about the universe since the creation.
 * Must be extended with a DB specific implementation.
 */
public class PlayerGameBoard implements Serializable, IGameBoard
{
	private static final long serialVersionUID = 1L;
	
	private int localTurn = 0;
	
	public int getLocalTurn()
	{
		return localTurn;
	}
	
	public void incLocalTurn()
	{
		++localTurn;
	}
	
	private final SEPCommonDB commonDB;
	
	public PlayerGameBoard(SEPCommonDB commonDB)
	{
		this.commonDB = commonDB;
	}
	
	public IGameConfig getConfig()
	{
		return commonDB.getConfig();
	}
	
	public SEPCommonDB getDB()
	{
		return commonDB;
	}
	
	public int getTurn()
	{
		return getConfig().getTurn();
	}
	
	@Override
	public Set<org.axan.sep.common.Player> getPlayers() throws GameBoardException
	{
		try
		{
			Set<IPlayer> players = Player.select(commonDB, IPlayer.class, null, null);
			Set<IPlayerConfig> playerConfigs = PlayerConfig.select(commonDB, IPlayerConfig.class, null, null);
			
			Set<org.axan.sep.common.Player> result = new HashSet<org.axan.sep.common.Player>();							
			
			
			for(IPlayer p : players)
			{
				for(IPlayerConfig pc : playerConfigs)
				{
					if (p.getName().matches(pc.getName()))
					{
						// TODO: image, portrait...
						result.add(new org.axan.sep.common.Player(p.getName(), new org.axan.sep.common.PlayerConfig(Basic.stringToColor(pc.getColor()), null, null)));
						break;
					}
				}
			}
			
			return result;
		}
		catch(SQLDataBaseException e)
		{
			throw new GameBoardException(e);
		}
	}
	
	public void undo() throws GameBoardException
	{
		// TODO: PlayerGameBoard.undo (IGameBoard.undo ?)
		throw new GameBoardException("Not implemented yet");
	}
	
	public void redo() throws GameBoardException
	{
		// TODO: PlayerGameBoard.redo (IGameBoard.redo ?)
		throw new GameBoardException("Not implemented yet");
	}
	
	public void resetTurn() throws GameBoardException
	{
		// TODO: PlayerGameBoard.resetTurn (IGameBoard.resetTurn ?)
		throw new GameBoardException("Not implemented yet");
	}
	
	public void endTurn() throws GameBoardException
	{
		// TODO: PlayerGameBoard.endTurn (IGameBoard.endTurn ?)
		throw new GameBoardException("Not implemented yet");
	}
	
	@Override
	public PlayerGameBoard build(String playerLogin, String celestialBodyName, eBuildingType buildingType) throws GameBoardException
	{
		// TODO Auto-generated method stub
		return this;
	}

}
