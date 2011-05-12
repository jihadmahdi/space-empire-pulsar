package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseUnitArrivalLog;
import org.axan.sep.common.db.IUnitArrivalLog;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
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

			if (where != null && params != null) where = String.format(where, params);
			String versionFilter;
			if (maxVersion)
			{
				versionFilter = String.format("(UnitArrivalLog.turn = ( SELECT MAX(LVUnitArrivalLog.turn) FROM UnitArrivalLog LVUnitArrivalLog WHERE LVUnitArrivalLog.owner = UnitArrivalLog.owner AND LVUnitArrivalLog.unitName = UnitArrivalLog.unitName AND LVUnitArrivalLog.turn = UnitArrivalLog.turn AND LVUnitArrivalLog.unitType = UnitArrivalLog.unitType AND LVUnitArrivalLog.instantTime = UnitArrivalLog.instantTime AND LVUnitArrivalLog.destination = UnitArrivalLog.destination AND LVUnitArrivalLog.vortex = UnitArrivalLog.vortex%s ))", (version != null && version >= 0) ? " AND LVUnitArrivalLog.turn <= "+version : "");
			}
			else
			{
				versionFilter = (version == null) ? "" : String.format("(UnitArrivalLog.turn = %d)", version);
			}
			where = String.format("%s%s", (where != null && !where.isEmpty()) ? "("+where+") AND " : "", versionFilter);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT UnitArrivalLog.* FROM UnitArrivalLog%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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


	public static <T extends IUnitArrivalLog> void insertOrUpdate(SQLiteConnection conn, T unitArrivalLog) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT owner FROM UnitArrivalLog WHERE owner = %s AND unitName = %s AND turn = %s AND unitType = %s AND instantTime = %s) AS exist ;", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO UnitArrivalLog (owner, unitName, turn, unitType, instantTime, destination, vortex) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'", "'"+unitArrivalLog.getDestination()+"'", "'"+unitArrivalLog.getVortex()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE UnitArrivalLog SET  destination = %s,  vortex = %s WHERE  owner = %s AND unitName = %s AND turn = %s AND unitType = %s AND instantTime = %s ;", "'"+unitArrivalLog.getDestination()+"'", "'"+unitArrivalLog.getVortex()+"'", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
