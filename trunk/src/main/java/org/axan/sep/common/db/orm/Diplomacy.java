package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseDiplomacy;
import org.axan.sep.common.db.orm.base.BaseDiplomacy;
import org.axan.sep.common.db.IDiplomacy;
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

public class Diplomacy implements IDiplomacy
{
	private final IBaseDiplomacy baseDiplomacyProxy;

	Diplomacy(IBaseDiplomacy baseDiplomacyProxy)
	{
		this.baseDiplomacyProxy = baseDiplomacyProxy;
	}

	public Diplomacy(String owner, String target, Integer turn, Boolean allowToLand, String foreignPolicy)
	{
		this(new BaseDiplomacy(owner, target, turn, allowToLand, foreignPolicy));
	}

	public Diplomacy(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseDiplomacy(stmnt));
	}

	public String getOwner()
	{
		return baseDiplomacyProxy.getOwner();
	}

	public String getTarget()
	{
		return baseDiplomacyProxy.getTarget();
	}

	public Integer getTurn()
	{
		return baseDiplomacyProxy.getTurn();
	}

	public Boolean getAllowToLand()
	{
		return baseDiplomacyProxy.getAllowToLand();
	}

	public String getForeignPolicy()
	{
		return baseDiplomacyProxy.getForeignPolicy();
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends IDiplomacy> Set<T> selectMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IDiplomacy> Set<T> selectVersion(SEPCommonDB db, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IDiplomacy> Set<T> selectUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IDiplomacy> Set<T> select(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Diplomacy.class : expectedType, stmnt));
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
	public static <T extends IDiplomacy> boolean existMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IDiplomacy> boolean existVersion(SEPCommonDB db,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IDiplomacy> boolean existUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IDiplomacy> boolean exist(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IDiplomacy> String selectQuery(Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		String versionFilter;
		if (maxVersion)
		{
			versionFilter = String.format("(Diplomacy.turn = ( SELECT MAX(LVDiplomacy.turn) FROM Diplomacy LVDiplomacy WHERE LVDiplomacy.owner = Diplomacy.owner AND LVDiplomacy.target = Diplomacy.target AND LVDiplomacy.turn = Diplomacy.turn AND LVDiplomacy.allowToLand = Diplomacy.allowToLand AND LVDiplomacy.foreignPolicy = Diplomacy.foreignPolicy%s ))", (version != null && version >= 0) ? " AND LVDiplomacy.turn <= "+version : "");
		}
		else
		{
			versionFilter = (version == null) ? "" : String.format("(Diplomacy.turn = %d)", version);
		}
		if (versionFilter != null && !versionFilter.isEmpty()) where = (where == null) ? versionFilter : String.format("%s AND %s", where, versionFilter);
		return String.format("SELECT Diplomacy.* FROM Diplomacy%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IDiplomacy> void insertOrUpdate(SEPCommonDB db, T diplomacy) throws SQLDataBaseException
	{
		try
		{
			boolean exist = existUnversioned(db, diplomacy.getClass(), null, " Diplomacy.owner = %s AND Diplomacy.target = %s AND Diplomacy.turn = %s", "'"+diplomacy.getOwner()+"'", "'"+diplomacy.getTarget()+"'", "'"+diplomacy.getTurn()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE Diplomacy SET allowToLand = %s,  foreignPolicy = %s WHERE  Diplomacy.owner = %s AND Diplomacy.target = %s AND Diplomacy.turn = %s ;", "'"+diplomacy.getAllowToLand()+"'", "'"+diplomacy.getForeignPolicy()+"'", "'"+diplomacy.getOwner()+"'", "'"+diplomacy.getTarget()+"'", "'"+diplomacy.getTurn()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO Diplomacy (owner, target, turn, allowToLand, foreignPolicy) VALUES (%s, %s, %s, %s, %s);", "'"+diplomacy.getOwner()+"'", "'"+diplomacy.getTarget()+"'", "'"+diplomacy.getTurn()+"'", "'"+diplomacy.getAllowToLand()+"'", "'"+diplomacy.getForeignPolicy()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
