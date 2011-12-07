package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.db.IStarshipTemplate;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseStarshipTemplate;
import org.axan.sep.common.db.orm.base.IBaseStarshipTemplate;

public class StarshipTemplate implements IStarshipTemplate
{
	private final IBaseStarshipTemplate baseStarshipTemplateProxy;

	StarshipTemplate(IBaseStarshipTemplate baseStarshipTemplateProxy)
	{
		this.baseStarshipTemplateProxy = baseStarshipTemplateProxy;
	}

	public StarshipTemplate(String name, String specializedClass)
	{
		this(new BaseStarshipTemplate(name, specializedClass));
	}

	public StarshipTemplate(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseStarshipTemplate(stmnt));
	}

	@Override
	public String getName()
	{
		return baseStarshipTemplateProxy.getName();
	}

	@Override
	public String getSpecializedClass()
	{
		return baseStarshipTemplateProxy.getSpecializedClass();
	}

	public static <T extends IStarshipTemplate> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"(1) ":"("+where+") ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends IStarshipTemplate> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
			}, params);
			while(stmnt.step())
			{
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) StarshipTemplate.class : expectedType, stmnt));
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

	public static <T extends IStarshipTemplate> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
			}, params);
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}


	private static <T extends IStarshipTemplate> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null && !where.isEmpty() && where.charAt(0) != '(') where = "("+where+")";
		return String.format("SELECT StarshipTemplate.* FROM StarshipTemplate%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IStarshipTemplate> void insertOrUpdate(SEPCommonDB db, T starshipTemplate) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, starshipTemplate.getClass(), null, " StarshipTemplate.name = %s", "'"+starshipTemplate.getName()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE StarshipTemplate SET specializedClass = %s WHERE  StarshipTemplate.name = %s ;", "'"+starshipTemplate.getSpecializedClass()+"'", "'"+starshipTemplate.getName()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO StarshipTemplate (name, specializedClass) VALUES (%s, %s);", "'"+starshipTemplate.getName()+"'", "'"+starshipTemplate.getSpecializedClass()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
