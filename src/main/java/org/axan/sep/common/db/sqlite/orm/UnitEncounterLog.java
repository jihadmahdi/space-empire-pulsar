package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseUnitEncounterLog;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;

public class UnitEncounterLog implements IUnitEncounterLog
{
	private final BaseUnitEncounterLog baseUnitEncounterLogProxy;

	public UnitEncounterLog(String unitOwner, String unitName, Integer unitTurn, String unitType, Integer instantTime, String seenOwner, String seenName, Integer seenTurn, String seenType)
	{
		baseUnitEncounterLogProxy = new BaseUnitEncounterLog(unitOwner, unitName, unitTurn, unitType, instantTime, seenOwner, seenName, seenTurn, seenType);
	}

	public UnitEncounterLog(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseUnitEncounterLogProxy = new BaseUnitEncounterLog(stmnt);
	}

	public String getUnitOwner()
	{
		return baseUnitEncounterLogProxy.getUnitOwner();
	}

	public String getUnitName()
	{
		return baseUnitEncounterLogProxy.getUnitName();
	}

	public Integer getUnitTurn()
	{
		return baseUnitEncounterLogProxy.getUnitTurn();
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

	public static <T extends IUnitEncounterLog> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM UnitEncounterLog%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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


	public static <T extends IUnitEncounterLog> void insertOrUpdate(SQLiteConnection conn, T unitEncounterLog) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT unitOwner FROM UnitEncounterLog WHERE unitOwner = %s AND unitName = %s AND unitTurn = %s AND unitType = %s AND instantTime = %s) AS exist ;", "'"+unitEncounterLog.getUnitOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getUnitTurn()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getInstantTime()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO UnitEncounterLog (unitOwner, unitName, unitTurn, unitType, instantTime, seenOwner, seenName, seenTurn, seenType) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s);", "'"+unitEncounterLog.getUnitOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getUnitTurn()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getInstantTime()+"'", "'"+unitEncounterLog.getSeenOwner()+"'", "'"+unitEncounterLog.getSeenName()+"'", "'"+unitEncounterLog.getSeenTurn()+"'", "'"+unitEncounterLog.getSeenType()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE UnitEncounterLog SET  seenOwner = %s,  seenName = %s,  seenTurn = %s,  seenType = %s WHERE  unitOwner = %s AND unitName = %s AND unitTurn = %s AND unitType = %s AND instantTime = %s ;", "'"+unitEncounterLog.getSeenOwner()+"'", "'"+unitEncounterLog.getSeenName()+"'", "'"+unitEncounterLog.getSeenTurn()+"'", "'"+unitEncounterLog.getSeenType()+"'", "'"+unitEncounterLog.getUnitOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getUnitTurn()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getInstantTime()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
