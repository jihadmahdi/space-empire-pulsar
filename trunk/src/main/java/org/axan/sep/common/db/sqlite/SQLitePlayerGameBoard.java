package org.axan.sep.common.db.sqlite;

import java.util.HashSet;
import java.util.Set;

import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.utils.Basic;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.sqlite.orm.Player;
import org.axan.sep.common.db.sqlite.orm.PlayerConfig;
import org.axan.sep.common.PlayerGameBoard;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteJob;

public class SQLitePlayerGameBoard extends PlayerGameBoard
{
	private static final long serialVersionUID = 1L;
	
	private final SEPCommonSQLiteDB commonDB;
	private transient SQLiteDB db;
	
	public SQLitePlayerGameBoard(SEPCommonSQLiteDB commonDB)
	{
		this.commonDB = commonDB;
		this.db = commonDB.getDB();
	}
	
	public IGameConfig getConfig()
	{
		return commonDB.getConfig();
	}
	
	public SQLiteDB getDB()
	{
		return commonDB.getDB();
	}
	
	@Override
	public int getTurn()
	{
		return getConfig().getTurn();
	}
	
	@Override
	public Set<org.axan.sep.common.Player> getPlayers() throws GameBoardException
	{
		try
		{
			return getDB().exec(new SQLiteJob<Set<org.axan.sep.common.Player>>()
			{
				@Override
				protected Set<org.axan.sep.common.Player> job(SQLiteConnection conn) throws Throwable
				{
					Set<org.axan.sep.common.Player> result = new HashSet<org.axan.sep.common.Player>();
					Set<IPlayer> players = Player.select(conn, getConfig(), IPlayer.class, null, null);
					Set<IPlayerConfig> playerConfigs = PlayerConfig.select(conn, getConfig(), IPlayerConfig.class, null, null);
					
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
			});
		}
		catch(SQLiteDBException e)
		{
			throw new GameBoardException(e);
		}
	}	
	
	@Override
	public SQLitePlayerGameBoard build(String playerLogin, String celestialBodyName, eBuildingType buildingType) throws GameBoardException
	{
		// TODO Auto-generated method stub
		return this;
	}
}
