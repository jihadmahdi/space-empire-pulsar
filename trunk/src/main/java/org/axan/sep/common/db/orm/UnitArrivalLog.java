package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseUnitArrivalLog;
import org.axan.sep.common.db.orm.base.BaseUnitArrivalLog;
import org.axan.sep.common.db.IUnitArrivalLog;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.SEPCommonDB;

public class UnitArrivalLog implements IUnitArrivalLog
{
	private final IBaseUnitArrivalLog baseUnitArrivalLogProxy;

	UnitArrivalLog(IBaseUnitArrivalLog baseUnitArrivalLogProxy)
	{
		this.baseUnitArrivalLogProxy = baseUnitArrivalLogProxy;
	}

	public UnitArrivalLog(String owner, String unitName, Integer turn, String unitType, Integer instantTime, String destination, String vortex)
	{
		this(new BaseUnitArrivalLog(owner, unitName, turn, unitType, instantTime, destination, vortex));
	}

	public UnitArrivalLog(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseUnitArrivalLog(stmnt));
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
	public static <T extends IUnitArrivalLog> Set<T> selectMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IUnitArrivalLog> Set<T> selectVersion(SEPCommonDB db, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IUnitArrivalLog> Set<T> selectUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IUnitArrivalLog> Set<T> select(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		ISQLDataBaseStatement stmnt = null;
		try
		{
			Set<T> results = new HashSet<T>();
			stmnt = db.getDB().prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+";", new ISQLDataBaseStatementJob<ISQLDataBaseStatement>()
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

	/** Set maxVersion to null to select last version. */
	public static <T extends IUnitArrivalLog> boolean existMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IUnitArrivalLog> boolean existVersion(SEPCommonDB db,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IUnitArrivalLog> boolean existUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IUnitArrivalLog> boolean exist(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		try
		{
			return db.getDB().prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+" ;", new ISQLDataBaseStatementJob<Boolean>()
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

	public static <T extends IUnitArrivalLog> void insertOrUpdate(SEPCommonDB db, T unitArrivalLog) throws SQLDataBaseException
	{
		try
		{
			boolean exist = existUnversioned(db, unitArrivalLog.getClass(), null, " UnitArrivalLog.owner = %s AND UnitArrivalLog.unitName = %s AND UnitArrivalLog.turn = %s AND UnitArrivalLog.unitType = %s AND UnitArrivalLog.instantTime = %s", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE UnitArrivalLog SET destination = %s,  vortex = %s WHERE  UnitArrivalLog.owner = %s AND UnitArrivalLog.unitName = %s AND UnitArrivalLog.turn = %s AND UnitArrivalLog.unitType = %s AND UnitArrivalLog.instantTime = %s ;", "'"+unitArrivalLog.getDestination()+"'", "'"+unitArrivalLog.getVortex()+"'", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO UnitArrivalLog (owner, unitName, turn, unitType, instantTime, destination, vortex) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+unitArrivalLog.getOwner()+"'", "'"+unitArrivalLog.getUnitName()+"'", "'"+unitArrivalLog.getTurn()+"'", "'"+unitArrivalLog.getUnitType()+"'", "'"+unitArrivalLog.getInstantTime()+"'", "'"+unitArrivalLog.getDestination()+"'", "'"+unitArrivalLog.getVortex()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
