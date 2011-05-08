package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseGovernment;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;

public class Government implements IGovernment
{
	private final BaseGovernment baseGovernmentProxy;

	public Government(String owner, Integer turn, String fleetName, Integer fleetTurn, String planetName, Integer planetTurn)
	{
		baseGovernmentProxy = new BaseGovernment(owner, turn, fleetName, fleetTurn, planetName, planetTurn);
	}

	public Government(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseGovernmentProxy = new BaseGovernment(stmnt);
	}

	public String getOwner()
	{
		return baseGovernmentProxy.getOwner();
	}

	public Integer getTurn()
	{
		return baseGovernmentProxy.getTurn();
	}

	public String getFleetName()
	{
		return baseGovernmentProxy.getFleetName();
	}

	public Integer getFleetTurn()
	{
		return baseGovernmentProxy.getFleetTurn();
	}

	public String getPlanetName()
	{
		return baseGovernmentProxy.getPlanetName();
	}

	public Integer getPlanetTurn()
	{
		return baseGovernmentProxy.getPlanetTurn();
	}

	public static <T extends IGovernment> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM Government%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	public static <T extends IGovernment> void insertOrUpdate(SQLiteConnection conn, T government) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT owner FROM Government WHERE owner = %s AND turn = %s) AS exist ;", "'"+government.getOwner()+"'", "'"+government.getTurn()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO Government (owner, turn, fleetName, fleetTurn, planetName, planetTurn) VALUES (%s, %s, %s, %s, %s, %s);", "'"+government.getOwner()+"'", "'"+government.getTurn()+"'", "'"+government.getFleetName()+"'", "'"+government.getFleetTurn()+"'", "'"+government.getPlanetName()+"'", "'"+government.getPlanetTurn()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE Government SET  fleetName = %s,  fleetTurn = %s,  planetName = %s,  planetTurn = %s WHERE  owner = %s AND turn = %s ;", "'"+government.getFleetName()+"'", "'"+government.getFleetTurn()+"'", "'"+government.getPlanetName()+"'", "'"+government.getPlanetTurn()+"'", "'"+government.getOwner()+"'", "'"+government.getTurn()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
