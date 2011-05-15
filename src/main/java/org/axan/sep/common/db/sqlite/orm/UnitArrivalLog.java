package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseUnitArrivalLog;
import org.axan.sep.common.db.IUnitArrivalLog;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

public class UnitArrivalLog implements IUnitArrivalLog
{
	private final BaseUnitArrivalLog baseUnitArrivalLogProxy;

	public UnitArrivalLog(String owner, String unitName, Integer turn, String unitType, Integer instantTime, String destination, String vortex)
	{
		baseUnitArrivalLogProxy = new BaseUnitArrivalLog(owner, unitName, turn, unitType, instantTime, destination, vortex);
	}

	public UnitArrivalLog(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseUnitArrivalLogProxy = new BaseUnitArrivalLog(stmnt);
	}

	public String getOwner()
	{
		return baseUnitArrivalLogProxy.getOwner();
	}

	public String getUnitName()
	{
		return baseUnitArrivalLogProxy.getUnitName();
	}

	public Integer getTurn()
	{
		return baseUnitArrivalLogProxy.getTurn();
	}

	public String getUnitType()
	{
		return baseUnitArrivalLogProxy.getUnitType();
	}

	public Integer getInstantTime()
	{
		return baseUnitArrivalLogProxy.getInstantTime();
	}

	public String getDestination()
	{
		return baseUnitArrivalLogProxy.getDestination();
	}

	public String getVortex()
	{
		return baseUnitArrivalLogProxy.getVortex();
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends IUnitArrivalLog> Set<T> selectMaxVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IUnitArrivalLog> Set<T> selectVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, version, from, where, params);
	}

	public static <T extends IUnitArrivalLog> Set<T> selectUnversioned(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, null, from, where, params);
	}

	private static <T extends IUnitArrivalLog> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();
			SQLiteStatement stmnt = conn.prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+";");
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) UnitArrivalLog.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends IUnitArrivalLog> boolean existMaxVersion(SQLiteConnection conn, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IUnitArrivalLog> boolean existVersion(SQLiteConnection conn,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, false, version, from, where, params);
	}

	public static <T extends IUnitArrivalLog> boolean existUnversioned(SQLiteConnection conn, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return exist(conn, expectedType, false, null, from, where, params);
	}

	private static <T extends IUnitArrivalLog> boolean exist(SQLiteConnection conn, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
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


	private static <T extends IUnitArrivalLog> String selectQuery(Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		String versionFilter;
		if (maxVersion)
		{
			versionFilter = String.format("(UnitArrivalLog.turn = ( SELECT MAX(LVUnitArrivalLog.turn) FROM UnitArrivalLog LVUnitArrivalLog WHERE LVUnitArrivalLog.owner = UnitArrivalLog.owner AND LVUnitArrivalLog.unitName = UnitArrivalLog.unitName AND LVUnitArrivalLog.turn = UnitArrivalLog.turn AND LVUnitArrivalLog.unitType = UnitArrivalLog.unitType AND LVUnitArrivalLog.instantTime = UnitArrivalLog.instantTime AND LVUnitArrivalLog.destination = UnitArrivalLog.destination AND LVUnitArrivalLog.vortex = UnitArrivalLog.vortex%s ))", (version != null && version >= 0) ? " AND LVUnitArrivalLog.turn <= "+version : "");
		}
		else
		{
			versionFilter = (version == null) ? "" : String.format("(UnitArrivalLog.turn = %d)", version);
		}
		if (versionFilter != null && !versionFilter.isEmpty()) where = (where == null) ? versionFilter : String.format("%s AND %s", where, versionFilter);
		return String.format("SELECT UnitArrivalLog.* FROM UnitArrivalLog%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IUnitArrivalLog> void insertOrUpdate(SQLiteConnection conn, T unitArrivalLog) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT owner FROM UnitArrivalLog WHERE owner = %s AND unitName = %s AND turn = %s AND unitType = %s AND instantTime = %s) AS exist ;", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO UnitArrivalLog (owner, unitName, turn, unitType, instantTime, destination, vortex) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'", "'"+unitArrivalLog.getDestination()+"'", "'"+unitArrivalLog.getVortex()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				conn.exec(String.format("UPDATE UnitArrivalLog SET destination = %s,  vortex = %s WHERE  owner = %s AND unitName = %s AND turn = %s AND unitType = %s AND instantTime = %s ;", "'"+unitArrivalLog.getDestination()+"'", "'"+unitArrivalLog.getVortex()+"'", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
