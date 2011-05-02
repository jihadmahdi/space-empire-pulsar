package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.BaseCarbonOrder;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public class CarbonOrder implements ICarbonOrder
{
	private final BaseCarbonOrder baseCarbonOrderProxy;

	public CarbonOrder(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseCarbonOrderProxy = new BaseCarbonOrder(stmnt);
	}

	public String getSource()
	{
		return baseCarbonOrderProxy.getSource();
	}

	public Integer getAmount()
	{
		return baseCarbonOrderProxy.getAmount();
	}

	public Integer getPriority()
	{
		return baseCarbonOrderProxy.getPriority();
	}

	public String getOwner()
	{
		return baseCarbonOrderProxy.getOwner();
	}

	public String getDestination()
	{
		return baseCarbonOrderProxy.getDestination();
	}

	public static <T extends ICarbonOrder> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM CarbonOrder%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}

}
