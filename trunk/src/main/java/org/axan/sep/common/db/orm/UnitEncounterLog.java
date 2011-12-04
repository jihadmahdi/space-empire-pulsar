package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseUnitEncounterLog;
import org.axan.sep.common.db.orm.base.BaseUnitEncounterLog;
import org.axan.sep.common.db.IUnitEncounterLog;
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

public class UnitEncounterLog implements IUnitEncounterLog
{
	private final IBaseUnitEncounterLog baseUnitEncounterLogProxy;

	UnitEncounterLog(IBaseUnitEncounterLog baseUnitEncounterLogProxy)
	{
		this.baseUnitEncounterLogProxy = baseUnitEncounterLogProxy;
	}

	public UnitEncounterLog(String owner, String unitName, Integer turn, String unitType, Integer instantTime, String seenOwner, String seenName, Integer seenTurn, String seenType)
	{
		this(new BaseUnitEncounterLog(owner, unitName, turn, unitType, instantTime, seenOwner, seenName, seenTurn, seenType));
	}

	public UnitEncounterLog(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseUnitEncounterLog(stmnt));
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
	public static <T extends IUnitEncounterLog> Set<T> selectMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IUnitEncounterLog> Set<T> selectVersion(SEPCommonDB db, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IUnitEncounterLog> Set<T> selectUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IUnitEncounterLog> Set<T> select(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
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

	/** Set maxVersion to null to select last version. */
	public static <T extends IUnitEncounterLog> boolean existMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IUnitEncounterLog> boolean existVersion(SEPCommonDB db,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IUnitEncounterLog> boolean existUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IUnitEncounterLog> boolean exist(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IUnitEncounterLog> String selectQuery(Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		String versionFilter;
		if (maxVersion)
		{
			versionFilter = String.format("(UnitEncounterLog.turn = ( SELECT MAX(LVUnitEncounterLog.turn) FROM UnitEncounterLog LVUnitEncounterLog WHERE LVUnitEncounterLog.owner = UnitEncounterLog.owner AND LVUnitEncounterLog.unitName = UnitEncounterLog.unitName AND LVUnitEncounterLog.turn = UnitEncounterLog.turn AND LVUnitEncounterLog.unitType = UnitEncounterLog.unitType AND LVUnitEncounterLog.instantTime = UnitEncounterLog.instantTime AND LVUnitEncounterLog.seenOwner = UnitEncounterLog.seenOwner AND LVUnitEncounterLog.seenName = UnitEncounterLog.seenName AND LVUnitEncounterLog.seenTurn = UnitEncounterLog.seenTurn AND LVUnitEncounterLog.seenType = UnitEncounterLog.seenType%s ))", (version != null && version >= 0) ? " AND LVUnitEncounterLog.turn <= "+version : "");
		}
		else
		{
			versionFilter = (version == null) ? "" : String.format("(UnitEncounterLog.turn = %d)", version);
		}
		if (versionFilter != null && !versionFilter.isEmpty()) where = (where == null) ? versionFilter : String.format("%s AND %s", where, versionFilter);
		return String.format("SELECT UnitEncounterLog.* FROM UnitEncounterLog%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IUnitEncounterLog> void insertOrUpdate(SEPCommonDB db, T unitEncounterLog) throws SQLDataBaseException
	{
		try
		{
			boolean exist = existUnversioned(db, unitEncounterLog.getClass(), null, " UnitEncounterLog.owner = %s AND UnitEncounterLog.unitName = %s AND UnitEncounterLog.turn = %s AND UnitEncounterLog.unitType = %s AND UnitEncounterLog.instantTime = %s", "'"+unitEncounterLog.getOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getTurn()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getInstantTime()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE UnitEncounterLog SET seenOwner = %s,  seenName = %s,  seenTurn = %s,  seenType = %s WHERE  UnitEncounterLog.owner = %s AND UnitEncounterLog.unitName = %s AND UnitEncounterLog.turn = %s AND UnitEncounterLog.unitType = %s AND UnitEncounterLog.instantTime = %s ;", "'"+unitEncounterLog.getSeenOwner()+"'", "'"+unitEncounterLog.getSeenName()+"'", "'"+unitEncounterLog.getSeenTurn()+"'", "'"+unitEncounterLog.getSeenType()+"'", "'"+unitEncounterLog.getOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getTurn()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getInstantTime()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO UnitEncounterLog (owner, unitName, turn, unitType, instantTime, seenOwner, seenName, seenTurn, seenType) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s);", "'"+unitEncounterLog.getOwner()+"'", "'"+unitEncounterLog.getUnitName()+"'", "'"+unitEncounterLog.getTurn()+"'", "'"+unitEncounterLog.getUnitType()+"'", "'"+unitEncounterLog.getInstantTime()+"'", "'"+unitEncounterLog.getSeenOwner()+"'", "'"+unitEncounterLog.getSeenName()+"'", "'"+unitEncounterLog.getSeenTurn()+"'", "'"+unitEncounterLog.getSeenType()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
