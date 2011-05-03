package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseUnitEncounterLog;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import com.almworks.sqlite4java.SQLiteConnection;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

public class UnitEncounterLog implements IUnitEncounterLog
{
	private final BaseUnitEncounterLog baseUnitEncounterLogProxy;

	public UnitEncounterLog(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseUnitEncounterLogProxy = new BaseUnitEncounterLog(stmnt);
	}

	public Integer getSeenTurn()
	{
		return baseUnitEncounterLogProxy.getSeenTurn();
	}

	public String getSeenOwner()
	{
		return baseUnitEncounterLogProxy.getSeenOwner();
	}

	public String getUnitType()
	{
		return baseUnitEncounterLogProxy.getUnitType();
	}

	public Integer getUnitTurn()
	{
		return baseUnitEncounterLogProxy.getUnitTurn();
	}

	public String getSeenType()
	{
		return baseUnitEncounterLogProxy.getSeenType();
	}

	public String getUnitName()
	{
		return baseUnitEncounterLogProxy.getUnitName();
	}

	public Integer getInstantTime()
	{
		return baseUnitEncounterLogProxy.getInstantTime();
	}

	public String getUnitOwner()
	{
		return baseUnitEncounterLogProxy.getUnitOwner();
	}

	public String getSeenName()
	{
		return baseUnitEncounterLogProxy.getSeenName();
	}

	public static <T extends IUnitEncounterLog> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
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

}
