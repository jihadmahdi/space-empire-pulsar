package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseUnitEncounterLog;
import org.axan.sep.common.db.IUnitEncounterLog;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

public class UnitEncounterLog implements IUnitEncounterLog
{
	private final BaseUnitEncounterLog baseUnitEncounterLogProxy;

	public UnitEncounterLog(String owner, String unitName, Integer turn, String unitType, Integer instantTime, String seenOwner, String seenName, Integer seenTurn, String seenType)
	{
		baseUnitEncounterLogProxy = new BaseUnitEncounterLog(owner, unitName, turn, unitType, instantTime, seenOwner, seenName, seenTurn, seenType);
	}

	public UnitEncounterLog(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseUnitEncounterLogProxy = new BaseUnitEncounterLog(stmnt);
	}

	public String getOwner()
	{
		return baseUnitEncounterLogProxy.getOwner();
	}

	public String getUnitName()
	{
		return baseUnitEncounterLogProxy.getUnitName();
	}

	public Integer getTurn()
	{
		return baseUnitEncounterLogProxy.getTurn();
	}

	public String getUnitType()
	{
		return baseUnitEncounterLogProxy.getUnitType();
	}

	public Integer getInstantTime()
	{
		return baseUnitEncounterLogProxy.getInstantTime();
	}

	public String getSeenOwner()
	{
		return baseUnitEncounterLogProxy.getSeenOwner();
	}

	public String getSeenName()
	{
		return baseUnitEncounterLogProxy.getSeenName();
	}

	public Integer getSeenTurn()
	{
		return baseUnitEncounterLogProxy.getSeenTurn();
	}

	public String getSeenType()
	{
		return baseUnitEncounterLogProxy.getSeenType();
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends IUnitEncounterLog> Set<T> selectMaxVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IUnitEncounterLog> Set<T> selectVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, version, from, where, params);
	}

	public static <T extends IUnitEncounterLog> Set<T> selectUnversioned(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, null, from, where, params);
	}

	private static <T extends IUnitEncounterLog> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			String versionFilter;
			if (maxVersion)
			{
				versionFilter = String.format("(UnitEncounterLog.turn = ( SELECT MAX(LVUnitEncounterLog.turn) FROM UnitEncounterLog LVUnitEncounterLog WHERE LVUnitEncounterLog.owner = UnitEncounterLog.owner AND LVUnitEncounterLog.unitName = UnitEncounterLog.unitName AND LVUnitEncounterLog.turn = UnitEncounterLog.turn AND LVUnitEncounterLog.unitType = UnitEncounterLog.unitType AND LVUnitEncounterLog.instantTime = UnitEncounterLog.instantTime AND LVUnitEncounterLog.seenOwner = UnitEncounterLog.seenOwner AND LVUnitEncounterLog.seenName = UnitEncounterLog.seenName AND LVUnitEncounterLog.seenTurn = UnitEncounterLog.seenTurn AND LVUnitEncounterLog.seenType = UnitEncounterLog.seenType%s ))", (version != null && version >= 0) ? " AND LVUnitEncounterLog.turn <= "+version : "");
			}
			else
			{
				versionFilter = (version == null) ? "" : String.format("(UnitEncounterLog.turn = %d)", version);
			}
			where = String.format("%s%s", (where != null && !where.isEmpty()) ? "("+where+") AND " : "", versionFilter);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT UnitEncounterLog.* FROM UnitEncounterLog%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) UnitEncounterLog.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	public static <T extends IUnitEncounterLog> void insertOrUpdate(SQLiteConnection conn, T unitEncounterLog) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT owner FROM UnitEncounterLog WHERE owner = %s AND unitName = %s AND turn = %s AND unitType = %s AND instantTime = %s) AS exist ;", "'"+unitEncounterLog.getOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getTurn()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getInstantTime()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO UnitEncounterLog (owner, unitName, turn, unitType, instantTime, seenOwner, seenName, seenTurn, seenType) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s);", "'"+unitEncounterLog.getOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getTurn()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getInstantTime()+"'", "'"+unitEncounterLog.getSeenOwner()+"'", "'"+unitEncounterLog.getSeenName()+"'", "'"+unitEncounterLog.getSeenTurn()+"'", "'"+unitEncounterLog.getSeenType()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE UnitEncounterLog SET  seenOwner = %s,  seenName = %s,  seenTurn = %s,  seenType = %s WHERE  owner = %s AND unitName = %s AND turn = %s AND unitType = %s AND instantTime = %s ;", "'"+unitEncounterLog.getSeenOwner()+"'", "'"+unitEncounterLog.getSeenName()+"'", "'"+unitEncounterLog.getSeenTurn()+"'", "'"+unitEncounterLog.getSeenType()+"'", "'"+unitEncounterLog.getOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getTurn()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getInstantTime()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
