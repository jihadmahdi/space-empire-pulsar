package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseCarbonOrder;
import org.axan.sep.common.db.ICarbonOrder;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.db.IGameConfig;

public class CarbonOrder implements ICarbonOrder
{
	private final BaseCarbonOrder baseCarbonOrderProxy;

	public CarbonOrder(String owner, String source, Integer priority, Integer amount, String destination)
	{
		baseCarbonOrderProxy = new BaseCarbonOrder(owner, source, priority, amount, destination);
	}

	public CarbonOrder(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseCarbonOrderProxy = new BaseCarbonOrder(stmnt);
	}

	public String getOwner()
	{
		return baseCarbonOrderProxy.getOwner();
	}

	public String getSource()
	{
		return baseCarbonOrderProxy.getSource();
	}

	public Integer getPriority()
	{
		return baseCarbonOrderProxy.getPriority();
	}

	public Integer getAmount()
	{
		return baseCarbonOrderProxy.getAmount();
	}

	public String getDestination()
	{
		return baseCarbonOrderProxy.getDestination();
	}

	public static <T extends ICarbonOrder> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT CarbonOrder.* FROM CarbonOrder%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) CarbonOrder.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	public static <T extends ICarbonOrder> void insertOrUpdate(SQLiteConnection conn, T carbonOrder) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT owner FROM CarbonOrder WHERE owner = %s AND source = %s AND priority = %s) AS exist ;", "'"+carbonOrder.getOwner()+"'", "'"+carbonOrder.getSource()+"'", "'"+carbonOrder.getPriority()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO CarbonOrder (owner, source, priority, amount, destination) VALUES (%s, %s, %s, %s, %s);", "'"+carbonOrder.getOwner()+"'", "'"+carbonOrder.getSource()+"'", "'"+carbonOrder.getPriority()+"'", "'"+carbonOrder.getAmount()+"'", "'"+carbonOrder.getDestination()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE CarbonOrder SET  amount = %s,  destination = %s WHERE  owner = %s AND source = %s AND priority = %s ;", "'"+carbonOrder.getAmount()+"'", "'"+carbonOrder.getDestination()+"'", "'"+carbonOrder.getOwner()+"'", "'"+carbonOrder.getSource()+"'", "'"+carbonOrder.getPriority()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
