package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseGovernment;
import org.axan.sep.common.db.IGovernment;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

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

	/** Set maxVersion to null to select last version. */
	public static <T extends IGovernment> Set<T> selectMaxVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IGovernment> Set<T> selectVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, version, from, where, params);
	}

	public static <T extends IGovernment> Set<T> selectUnversioned(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, null, from, where, params);
	}

	private static <T extends IGovernment> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			String versionFilter;
			if (maxVersion)
			{
				versionFilter = String.format("(Government.turn = ( SELECT MAX(LVGovernment.turn) FROM Government LVGovernment WHERE LVGovernment.owner = Government.owner AND LVGovernment.turn = Government.turn AND LVGovernment.fleetName = Government.fleetName AND LVGovernment.fleetTurn = Government.fleetTurn AND LVGovernment.planetName = Government.planetName AND LVGovernment.planetTurn = Government.planetTurn%s ))", (version != null && version >= 0) ? " AND LVGovernment.turn <= "+version : "");
			}
			else
			{
				versionFilter = (version == null) ? "" : String.format("(Government.turn = %d)", version);
			}
			where = String.format("%s%s", (where != null && !where.isEmpty()) ? "("+where+") AND " : "", versionFilter);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT Government.* FROM Government%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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
