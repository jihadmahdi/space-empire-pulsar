package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseMovePlan;
import org.axan.sep.common.db.orm.base.BaseMovePlan;
import org.axan.sep.common.db.IMovePlan;
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

public class MovePlan implements IMovePlan
{
	private final IBaseMovePlan baseMovePlanProxy;

	MovePlan(IBaseMovePlan baseMovePlanProxy)
	{
		this.baseMovePlanProxy = baseMovePlanProxy;
	}

	public MovePlan(String owner, String name, Integer turn, Integer priority, Integer delay, Boolean attack, String destination)
	{
		this(new BaseMovePlan(owner, name, turn, priority, delay, attack, destination));
	}

	public MovePlan(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseMovePlan(stmnt));
	}

	public String getOwner()
	{
		return baseMovePlanProxy.getOwner();
	}

	public String getName()
	{
		return baseMovePlanProxy.getName();
	}

	public Integer getTurn()
	{
		return baseMovePlanProxy.getTurn();
	}

	public Integer getPriority()
	{
		return baseMovePlanProxy.getPriority();
	}

	public Integer getDelay()
	{
		return baseMovePlanProxy.getDelay();
	}

	public Boolean getAttack()
	{
		return baseMovePlanProxy.getAttack();
	}

	public String getDestination()
	{
		return baseMovePlanProxy.getDestination();
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
			});
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
			});
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}


	private static <T extends IMovePlan> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT MovePlan.* FROM MovePlan%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IMovePlan> void insertOrUpdate(SEPCommonDB db, T movePlan) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, movePlan.getClass(), null, " MovePlan.owner = %s AND MovePlan.name = %s AND MovePlan.turn = %s AND MovePlan.priority = %s", "'"+movePlan.getOwner()+"'", "'"+movePlan.getName()+"'", "'"+movePlan.getTurn()+"'", "'"+movePlan.getPriority()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE MovePlan SET delay = %s,  attack = %s,  destination = %s WHERE  MovePlan.owner = %s AND MovePlan.name = %s AND MovePlan.turn = %s AND MovePlan.priority = %s ;", "'"+movePlan.getDelay()+"'", "'"+movePlan.getAttack()+"'", "'"+movePlan.getDestination()+"'", "'"+movePlan.getOwner()+"'", "'"+movePlan.getName()+"'", "'"+movePlan.getTurn()+"'", "'"+movePlan.getPriority()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO MovePlan (owner, name, turn, priority, delay, attack, destination) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+movePlan.getOwner()+"'", "'"+movePlan.getName()+"'", "'"+movePlan.getTurn()+"'", "'"+movePlan.getPriority()+"'", "'"+movePlan.getDelay()+"'", "'"+movePlan.getAttack()+"'", "'"+movePlan.getDestination()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
