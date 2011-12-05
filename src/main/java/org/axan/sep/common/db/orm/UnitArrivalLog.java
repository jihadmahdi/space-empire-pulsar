package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.db.IUnitArrivalLog;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseUnitArrivalLog;
import org.axan.sep.common.db.orm.base.IBaseUnitArrivalLog;

public class UnitArrivalLog implements IUnitArrivalLog
{
	private final IBaseUnitArrivalLog baseUnitArrivalLogProxy;

	UnitArrivalLog(IBaseUnitArrivalLog baseUnitArrivalLogProxy)
	{
		this.baseUnitArrivalLogProxy = baseUnitArrivalLogProxy;
	}

	public UnitArrivalLog(String owner, String unitName, String unitType, Integer logTurn, Integer instantTime, String destination, String vortex)
	{
		this(new BaseUnitArrivalLog(owner, unitName, unitType, logTurn, instantTime, destination, vortex));
	}

	public UnitArrivalLog(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseUnitArrivalLog(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseUnitArrivalLogProxy.getOwner();
	}

	@Override
	public String getUnitName()
	{
		return baseUnitArrivalLogProxy.getUnitName();
	}

	@Override
	public String getUnitType()
	{
		return baseUnitArrivalLogProxy.getUnitType();
	}

	@Override
	public Integer getLogTurn()
	{
		return baseUnitArrivalLogProxy.getLogTurn();
	}

	@Override
	public Integer getInstantTime()
	{
		return baseUnitArrivalLogProxy.getInstantTime();
	}

	@Override
	public String getDestination()
	{
		return baseUnitArrivalLogProxy.getDestination();
	}

	@Override
	public String getVortex()
	{
		return baseUnitArrivalLogProxy.getVortex();
	}

	public static <T extends IUnitArrivalLog> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"":where+" ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends IUnitArrivalLog> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		ISQLDataBaseStatement stmnt = null;
		try
		{
			Set<T> results = new HashSet<T>();
			stmnt = db.getDB().prepare(selectQuery(expectedType, from, where, params)+";", new ISQLDataBaseStatementJob<ISQLDataBaseStatement>()
			{
				@Override
				public ISQLDataBaseStatement job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
				{
					return stmnt;
				}
			});
			while(stmnt.step())
			{
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) UnitArrivalLog.class : expectedType, stmnt));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
		finally
		{
			if (stmnt != null) stmnt.dispose();
		}
	}

	public static <T extends IUnitArrivalLog> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		try
		{
			return db.getDB().prepare(selectQuery(expectedType, from, where, params)+" ;", new ISQLDataBaseStatementJob<Boolean>()
			{
				@Override
				public Boolean job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
				{
					try
					{
						return stmnt.step() && stmnt.columnValue(0) != null;
					}
					finally
					{
						if (stmnt != null) stmnt.dispose();
					}
				}
			});
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}


	private static <T extends IUnitArrivalLog> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT UnitArrivalLog.* FROM UnitArrivalLog%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IUnitArrivalLog> void insertOrUpdate(SEPCommonDB db, T unitArrivalLog) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, unitArrivalLog.getClass(), null, " UnitArrivalLog.owner = %s AND UnitArrivalLog.unitName = %s AND UnitArrivalLog.unitType = %s AND UnitArrivalLog.logTurn = %s AND UnitArrivalLog.instantTime = %s", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getLogTurn()+"'", "'"+unitArrivalLog.getInstantTime()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE UnitArrivalLog SET destination = %s,  vortex = %s WHERE  UnitArrivalLog.owner = %s AND UnitArrivalLog.unitName = %s AND UnitArrivalLog.unitType = %s AND UnitArrivalLog.logTurn = %s AND UnitArrivalLog.instantTime = %s ;", "'"+unitArrivalLog.getDestination()+"'", "'"+unitArrivalLog.getVortex()+"'", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getLogTurn()+"'", "'"+unitArrivalLog.getInstantTime()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO UnitArrivalLog (owner, unitName, unitType, logTurn, instantTime, destination, vortex) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getLogTurn()+"'", "'"+unitArrivalLog.getInstantTime()+"'", "'"+unitArrivalLog.getDestination()+"'", "'"+unitArrivalLog.getVortex()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
