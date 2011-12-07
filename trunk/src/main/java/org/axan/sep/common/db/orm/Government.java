package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.db.IGovernment;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseGovernment;
import org.axan.sep.common.db.orm.base.IBaseGovernment;

public class Government implements IGovernment
{
	private final IBaseGovernment baseGovernmentProxy;

	Government(IBaseGovernment baseGovernmentProxy)
	{
		this.baseGovernmentProxy = baseGovernmentProxy;
	}

	public Government(String owner, String fleetName, String planetName)
	{
		this(new BaseGovernment(owner, fleetName, planetName));
	}

	public Government(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseGovernment(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseGovernmentProxy.getOwner();
	}

	@Override
	public String getFleetName()
	{
		return baseGovernmentProxy.getFleetName();
	}

	@Override
	public String getPlanetName()
	{
		return baseGovernmentProxy.getPlanetName();
	}

	public static <T extends IGovernment> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"(1) ":"("+where+") ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends IGovernment> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Government.class : expectedType, stmnt));
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

	public static <T extends IGovernment> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IGovernment> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null && !where.isEmpty() && where.charAt(0) != '(') where = "("+where+")";
		return String.format("SELECT Government.* FROM Government%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IGovernment> void insertOrUpdate(SEPCommonDB db, T government) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, government.getClass(), null, " Government.owner = %s", "'"+government.getOwner()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE Government SET fleetName = %s,  planetName = %s WHERE  Government.owner = %s ;", "'"+government.getFleetName()+"'", "'"+government.getPlanetName()+"'", "'"+government.getOwner()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO Government (owner, fleetName, planetName) VALUES (%s, %s, %s);", "'"+government.getOwner()+"'", "'"+government.getFleetName()+"'", "'"+government.getPlanetName()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
