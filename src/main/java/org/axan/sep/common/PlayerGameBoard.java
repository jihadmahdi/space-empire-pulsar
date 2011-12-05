package org.axan.sep.common;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.axan.eplib.orm.SQLDataBaseException;
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
	public Map<IPlayer, IPlayerConfig> getPlayerList() throws GameBoardException
	{		
		try
		{
			Set<IPlayer> players = Player.select(commonDB, IPlayer.class, null, null);
			Set<IPlayerConfig> playerConfigs = PlayerConfig.select(commonDB, IPlayerConfig.class, null, null);
			
			Map<IPlayer, IPlayerConfig> result = new TreeMap<IPlayer, IPlayerConfig>();						
						
			for(IPlayer p : players)
			{
				for(IPlayerConfig pc : playerConfigs)
				{
					if (p.getName().matches(pc.getName()))
					{
						// TODO: image, portrait...
						result.put(p, pc);
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
}
