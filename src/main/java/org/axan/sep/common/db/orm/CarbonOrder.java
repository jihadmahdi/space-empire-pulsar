package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.db.ICarbonOrder;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseCarbonOrder;
import org.axan.sep.common.db.orm.base.IBaseCarbonOrder;

public class CarbonOrder implements ICarbonOrder
{
	private final IBaseCarbonOrder baseCarbonOrderProxy;

	CarbonOrder(IBaseCarbonOrder baseCarbonOrderProxy)
	{
		this.baseCarbonOrderProxy = baseCarbonOrderProxy;
	}

	public CarbonOrder(String owner, String source, Integer priority, Integer amount, String destination)
	{
		this(new BaseCarbonOrder(owner, source, priority, amount, destination));
	}

	public CarbonOrder(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseCarbonOrder(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseCarbonOrderProxy.getOwner();
	}

	@Override
	public String getSource()
	{
		return baseCarbonOrderProxy.getSource();
	}

	@Override
	public Integer getPriority()
	{
		return baseCarbonOrderProxy.getPriority();
	}

	@Override
	public Integer getAmount()
	{
		return baseCarbonOrderProxy.getAmount();
	}

	@Override
	public String getDestination()
	{
		return baseCarbonOrderProxy.getDestination();
	}

	public static <T extends ICarbonOrder> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"(1) ":"("+where+") ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends ICarbonOrder> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) CarbonOrder.class : expectedType, stmnt));
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

	public static <T extends ICarbonOrder> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends ICarbonOrder> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null && !where.isEmpty() && where.charAt(0) != '(') where = "("+where+")";
		return String.format("SELECT CarbonOrder.* FROM CarbonOrder%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends ICarbonOrder> void insertOrUpdate(SEPCommonDB db, T carbonOrder) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, carbonOrder.getClass(), null, " CarbonOrder.owner = %s AND CarbonOrder.source = %s AND CarbonOrder.priority = %s", "'"+carbonOrder.getOwner()+"'", "'"+carbonOrder.getSource()+"'", "'"+carbonOrder.getPriority()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE CarbonOrder SET amount = %s,  destination = %s WHERE  CarbonOrder.owner = %s AND CarbonOrder.source = %s AND CarbonOrder.priority = %s ;", "'"+carbonOrder.getAmount()+"'", "'"+carbonOrder.getDestination()+"'", "'"+carbonOrder.getOwner()+"'", "'"+carbonOrder.getSource()+"'", "'"+carbonOrder.getPriority()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO CarbonOrder (owner, source, priority, amount, destination) VALUES (%s, %s, %s, %s, %s);", "'"+carbonOrder.getOwner()+"'", "'"+carbonOrder.getSource()+"'", "'"+carbonOrder.getPriority()+"'", "'"+carbonOrder.getAmount()+"'", "'"+carbonOrder.getDestination()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
