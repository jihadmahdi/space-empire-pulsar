package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.db.IUnitEncounterLog;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseUnitEncounterLog;
import org.axan.sep.common.db.orm.base.IBaseUnitEncounterLog;

public class UnitEncounterLog implements IUnitEncounterLog
{
	private final IBaseUnitEncounterLog baseUnitEncounterLogProxy;

	UnitEncounterLog(IBaseUnitEncounterLog baseUnitEncounterLogProxy)
	{
		this.baseUnitEncounterLogProxy = baseUnitEncounterLogProxy;
	}

	public UnitEncounterLog(String owner, String unitName, String unitType, Integer logTurn, Integer instantTime, String seenOwner, String seenName, Integer seenTurn, String seenType)
	{
		this(new BaseUnitEncounterLog(owner, unitName, unitType, logTurn, instantTime, seenOwner, seenName, seenTurn, seenType));
	}

	public UnitEncounterLog(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseUnitEncounterLog(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseUnitEncounterLogProxy.getOwner();
	}

	@Override
	public String getUnitName()
	{
		return baseUnitEncounterLogProxy.getUnitName();
	}

	@Override
	public String getUnitType()
	{
		return baseUnitEncounterLogProxy.getUnitType();
	}

	@Override
	public Integer getLogTurn()
	{
		return baseUnitEncounterLogProxy.getLogTurn();
	}

	@Override
	public Integer getInstantTime()
	{
		return baseUnitEncounterLogProxy.getInstantTime();
	}

	@Override
	public String getSeenOwner()
	{
		return baseUnitEncounterLogProxy.getSeenOwner();
	}

	@Override
	public String getSeenName()
	{
		return baseUnitEncounterLogProxy.getSeenName();
	}

	@Override
	public Integer getSeenTurn()
	{
		return baseUnitEncounterLogProxy.getSeenTurn();
	}

	@Override
	public String getSeenType()
	{
		return baseUnitEncounterLogProxy.getSeenType();
	}

	public static <T extends IUnitEncounterLog> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"":where+" ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends IUnitEncounterLog> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) UnitEncounterLog.class : expectedType, stmnt));
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

	public static <T extends IUnitEncounterLog> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IUnitEncounterLog> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT UnitEncounterLog.* FROM UnitEncounterLog%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IUnitEncounterLog> void insertOrUpdate(SEPCommonDB db, T unitEncounterLog) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, unitEncounterLog.getClass(), null, " UnitEncounterLog.owner = %s AND UnitEncounterLog.unitName = %s AND UnitEncounterLog.unitType = %s AND UnitEncounterLog.logTurn = %s AND UnitEncounterLog.instantTime = %s", "'"+unitEncounterLog.getOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getLogTurn()+"'", "'"+unitEncounterLog.getInstantTime()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE UnitEncounterLog SET seenOwner = %s,  seenName = %s,  seenTurn = %s,  seenType = %s WHERE  UnitEncounterLog.owner = %s AND UnitEncounterLog.unitName = %s AND UnitEncounterLog.unitType = %s AND UnitEncounterLog.logTurn = %s AND UnitEncounterLog.instantTime = %s ;", "'"+unitEncounterLog.getSeenOwner()+"'", "'"+unitEncounterLog.getSeenName()+"'", "'"+unitEncounterLog.getSeenTurn()+"'", "'"+unitEncounterLog.getSeenType()+"'", "'"+unitEncounterLog.getOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getLogTurn()+"'", "'"+unitEncounterLog.getInstantTime()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO UnitEncounterLog (owner, unitName, unitType, logTurn, instantTime, seenOwner, seenName, seenTurn, seenType) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s);", "'"+unitEncounterLog.getOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getLogTurn()+"'", "'"+unitEncounterLog.getInstantTime()+"'", "'"+unitEncounterLog.getSeenOwner()+"'", "'"+unitEncounterLog.getSeenName()+"'", "'"+unitEncounterLog.getSeenTurn()+"'", "'"+unitEncounterLog.getSeenType()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
