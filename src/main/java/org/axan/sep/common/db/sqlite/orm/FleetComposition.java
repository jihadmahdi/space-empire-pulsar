package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseFleetComposition;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import com.almworks.sqlite4java.SQLiteConnection;
import java.util.HashSet;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;

public class FleetComposition implements IFleetComposition
{
	private final BaseFleetComposition baseFleetCompositionProxy;

	public FleetComposition(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseFleetCompositionProxy = new BaseFleetComposition(stmnt);
	}

	public String getFleetName()
	{
		return baseFleetCompositionProxy.getFleetName();
	}

	public Integer getFleetTurn()
	{
		return baseFleetCompositionProxy.getFleetTurn();
	}

	public String getFleetOwner()
	{
		return baseFleetCompositionProxy.getFleetOwner();
	}

	public Integer getQuantity()
	{
		return baseFleetCompositionProxy.getQuantity();
	}

	public String getStarshipTemplate()
	{
		return baseFleetCompositionProxy.getStarshipTemplate();
	}

	public static <T extends IFleetComposition> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM FleetComposition%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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
