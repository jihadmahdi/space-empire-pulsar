package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseGovernment;
import org.axan.sep.common.db.IGovernment;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Locale;
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
			SQLiteStatement stmnt = conn.prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+";");
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Government.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends IGovernment> boolean existMaxVersion(SQLiteConnection conn, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IGovernment> boolean existVersion(SQLiteConnection conn,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, false, version, from, where, params);
	}

	public static <T extends IGovernment> boolean existUnversioned(SQLiteConnection conn, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, false, null, from, where, params);
	}

	private static <T extends IGovernment> boolean exist(SQLiteConnection conn, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare("SELECT EXISTS ( "+selectQuery(expectedType, maxVersion, version, from, where, params) + " );");
			return stmnt.step() && stmnt.columnInt(0) != 0;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	private static <T extends IGovernment> String selectQuery(Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		String versionFilter;
		if (maxVersion)
		{
			versionFilter = String.format("(Government.turn = ( SELECT MAX(LVGovernment.turn) FROM Government LVGovernment WHERE LVGovernment.owner = Government.owner AND LVGovernment.turn = Government.turn AND LVGovernment.fleetName = Government.fleetName AND LVGovernment.fleetTurn = Government.fleetTurn AND LVGovernment.planetName = Government.planetName AND LVGovernment.planetTurn = Government.planetTurn%s ))", (version != null && version >= 0) ? " AND LVGovernment.turn <= "+version : "");
		}
		else
		{
			versionFilter = (version == null) ? "" : String.format("(Government.turn = %d)", version);
		}
		if (versionFilter != null && !versionFilter.isEmpty()) where = (where == null) ? versionFilter : String.format("%s AND %s", where, versionFilter);
		return String.format("SELECT Government.* FROM Government%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IGovernment> void insertOrUpdate(SQLiteConnection conn, T government) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT owner FROM Government WHERE owner = %s AND turn = %s) AS exist ;", "'"+government.getOwner()+"'", "'"+government.getTurn()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO Government (owner, turn, fleetName, fleetTurn, planetName, planetTurn) VALUES (%s, %s, %s, %s, %s, %s);", "'"+government.getOwner()+"'", "'"+government.getTurn()+"'", "'"+government.getFleetName()+"'", "'"+government.getFleetTurn()+"'", "'"+government.getPlanetName()+"'", "'"+government.getPlanetTurn()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				conn.exec(String.format("UPDATE Government SET fleetName = %s,  fleetTurn = %s,  planetName = %s,  planetTurn = %s WHERE  owner = %s AND turn = %s ;", "'"+government.getFleetName()+"'", "'"+government.getFleetTurn()+"'", "'"+government.getPlanetName()+"'", "'"+government.getPlanetTurn()+"'", "'"+government.getOwner()+"'", "'"+government.getTurn()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
