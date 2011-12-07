package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.db.IMovePlan;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseMovePlan;
import org.axan.sep.common.db.orm.base.IBaseMovePlan;

public class MovePlan implements IMovePlan
{
	private final IBaseMovePlan baseMovePlanProxy;

	MovePlan(IBaseMovePlan baseMovePlanProxy)
	{
		this.baseMovePlanProxy = baseMovePlanProxy;
	}

	public MovePlan(String owner, String name, Integer priority, Integer delay, Boolean attack, String destination)
	{
		this(new BaseMovePlan(owner, name, priority, delay, attack, destination));
	}

	public MovePlan(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseMovePlan(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseMovePlanProxy.getOwner();
	}

	@Override
	public String getName()
	{
		return baseMovePlanProxy.getName();
	}

	@Override
	public Integer getPriority()
	{
		return baseMovePlanProxy.getPriority();
	}

	@Override
	public Integer getDelay()
	{
		return baseMovePlanProxy.getDelay();
	}

	@Override
	public Boolean getAttack()
	{
		return baseMovePlanProxy.getAttack();
	}

	@Override
	public String getDestination()
	{
		return baseMovePlanProxy.getDestination();
	}

	public static <T extends IMovePlan> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"(1) ":"("+where+") ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends IMovePlan> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) MovePlan.class : expectedType, stmnt));
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

	public static <T extends IMovePlan> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IMovePlan> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null && !where.isEmpty() && where.charAt(0) != '(') where = "("+where+")";
		return String.format("SELECT MovePlan.* FROM MovePlan%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IMovePlan> void insertOrUpdate(SEPCommonDB db, T movePlan) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, movePlan.getClass(), null, " MovePlan.owner = %s AND MovePlan.name = %s AND MovePlan.priority = %s", "'"+movePlan.getOwner()+"'", "'"+movePlan.getName()+"'", "'"+movePlan.getPriority()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE MovePlan SET delay = %s,  attack = %s,  destination = %s WHERE  MovePlan.owner = %s AND MovePlan.name = %s AND MovePlan.priority = %s ;", "'"+movePlan.getDelay()+"'", "'"+movePlan.getAttack()+"'", "'"+movePlan.getDestination()+"'", "'"+movePlan.getOwner()+"'", "'"+movePlan.getName()+"'", "'"+movePlan.getPriority()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO MovePlan (owner, name, priority, delay, attack, destination) VALUES (%s, %s, %s, %s, %s, %s);", "'"+movePlan.getOwner()+"'", "'"+movePlan.getName()+"'", "'"+movePlan.getPriority()+"'", "'"+movePlan.getDelay()+"'", "'"+movePlan.getAttack()+"'", "'"+movePlan.getDestination()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
