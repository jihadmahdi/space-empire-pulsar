package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseUnitArrivalLog;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import com.almworks.sqlite4java.SQLiteConnection;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

public class UnitArrivalLog implements IUnitArrivalLog
{
	private final BaseUnitArrivalLog baseUnitArrivalLogProxy;

	public UnitArrivalLog(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseUnitArrivalLogProxy = new BaseUnitArrivalLog(stmnt);
	}

	public String getUnitType()
	{
		return baseUnitArrivalLogProxy.getUnitType();
	}

	public Integer getUnitTurn()
	{
		return baseUnitArrivalLogProxy.getUnitTurn();
	}

	public String getDestination()
	{
		return baseUnitArrivalLogProxy.getDestination();
	}

	public String getUnitName()
	{
		return baseUnitArrivalLogProxy.getUnitName();
	}

	public Integer getInstantTime()
	{
		return baseUnitArrivalLogProxy.getInstantTime();
	}

	public String getUnitOwner()
	{
		return baseUnitArrivalLogProxy.getUnitOwner();
	}

	public String getVortex()
	{
		return baseUnitArrivalLogProxy.getVortex();
	}

	public static <T extends IUnitArrivalLog> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM UnitArrivalLog%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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
