package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.db.IDiplomacy;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseDiplomacy;
import org.axan.sep.common.db.orm.base.IBaseDiplomacy;

public class Diplomacy implements IDiplomacy
{
	private final IBaseDiplomacy baseDiplomacyProxy;

	Diplomacy(IBaseDiplomacy baseDiplomacyProxy)
	{
		this.baseDiplomacyProxy = baseDiplomacyProxy;
	}

	public Diplomacy(String owner, String target, Boolean allowToLand, String foreignPolicy)
	{
		this(new BaseDiplomacy(owner, target, allowToLand, foreignPolicy));
	}

	public Diplomacy(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseDiplomacy(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseDiplomacyProxy.getOwner();
	}

	@Override
	public String getTarget()
	{
		return baseDiplomacyProxy.getTarget();
	}

	@Override
	public Boolean getAllowToLand()
	{
		return baseDiplomacyProxy.getAllowToLand();
	}

	@Override
	public String getForeignPolicy()
	{
		return baseDiplomacyProxy.getForeignPolicy();
	}

	public static <T extends IDiplomacy> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"":where+" ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends IDiplomacy> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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

	public static <T extends IDiplomacy> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IDiplomacy> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT Diplomacy.* FROM Diplomacy%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IDiplomacy> void insertOrUpdate(SEPCommonDB db, T diplomacy) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, diplomacy.getClass(), null, " Diplomacy.owner = %s AND Diplomacy.target = %s", "'"+diplomacy.getOwner()+"'", "'"+diplomacy.getTarget()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE Diplomacy SET allowToLand = %s,  foreignPolicy = %s WHERE  Diplomacy.owner = %s AND Diplomacy.target = %s ;", "'"+diplomacy.getAllowToLand()+"'", "'"+diplomacy.getForeignPolicy()+"'", "'"+diplomacy.getOwner()+"'", "'"+diplomacy.getTarget()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO Diplomacy (owner, target, allowToLand, foreignPolicy) VALUES (%s, %s, %s, %s);", "'"+diplomacy.getOwner()+"'", "'"+diplomacy.getTarget()+"'", "'"+diplomacy.getAllowToLand()+"'", "'"+diplomacy.getForeignPolicy()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
