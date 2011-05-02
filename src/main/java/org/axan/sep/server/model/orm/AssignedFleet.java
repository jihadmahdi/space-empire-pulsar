package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.BaseAssignedFleet;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public class AssignedFleet implements IAssignedFleet
{
	private final BaseAssignedFleet baseAssignedFleetProxy;

	public AssignedFleet(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseAssignedFleetProxy = new BaseAssignedFleet(stmnt);
	}

	public String getFleetName()
	{
		return baseAssignedFleetProxy.getFleetName();
	}

	public String getOwner()
	{
		return baseAssignedFleetProxy.getOwner();
	}

	public String getCelestialBody()
	{
		return baseAssignedFleetProxy.getCelestialBody();
	}

	public static <T extends IAssignedFleet> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM AssignedFleet%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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
