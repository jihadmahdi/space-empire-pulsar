package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseUnitArrivalLog;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;

public class UnitArrivalLog implements IUnitArrivalLog
{
	private final BaseUnitArrivalLog baseUnitArrivalLogProxy;

	public UnitArrivalLog(String unitOwner, String unitName, Integer unitTurn, String unitType, Integer instantTime, String destination, String vortex)
	{
		baseUnitArrivalLogProxy = new BaseUnitArrivalLog(unitOwner, unitName, unitTurn, unitType, instantTime, destination, vortex);
	}

	public UnitArrivalLog(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseUnitArrivalLogProxy = new BaseUnitArrivalLog(stmnt);
	}

	public String getUnitOwner()
	{
		return baseUnitArrivalLogProxy.getUnitOwner();
	}

	public String getUnitName()
	{
		return baseUnitArrivalLogProxy.getUnitName();
	}

	public Integer getUnitTurn()
	{
		return baseUnitArrivalLogProxy.getUnitTurn();
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

	public static <T extends IUnitArrivalLog> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String where, Object ... params) throws SQLiteDBException
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


	public static <T extends IUnitArrivalLog> void insertOrUpdate(SQLiteConnection conn, T unitArrivalLog) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT unitOwner FROM UnitArrivalLog WHERE unitOwner = %s AND unitName = %s AND unitTurn = %s AND unitType = %s AND instantTime = %s) AS exist ;", "'"+unitArrivalLog.getUnitOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getUnitTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO UnitArrivalLog (unitOwner, unitName, unitTurn, unitType, instantTime, destination, vortex) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+unitArrivalLog.getUnitOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getUnitTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'", "'"+unitArrivalLog.getDestination()+"'", "'"+unitArrivalLog.getVortex()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE UnitArrivalLog SET  destination = %s,  vortex = %s WHERE  unitOwner = %s AND unitName = %s AND unitTurn = %s AND unitType = %s AND instantTime = %s ;", "'"+unitArrivalLog.getDestination()+"'", "'"+unitArrivalLog.getVortex()+"'", "'"+unitArrivalLog.getUnitOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getUnitTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
