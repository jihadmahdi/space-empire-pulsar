package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseAssignedFleet;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;

public class AssignedFleet implements IAssignedFleet
{
	private final BaseAssignedFleet baseAssignedFleetProxy;

	public AssignedFleet(String celestialBody, String owner, String fleetName)
	{
		baseAssignedFleetProxy = new BaseAssignedFleet(celestialBody, owner, fleetName);
	}

	public AssignedFleet(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseAssignedFleetProxy = new BaseAssignedFleet(stmnt);
	}

	public String getCelestialBody()
	{
		return baseAssignedFleetProxy.getCelestialBody();
	}

	public String getOwner()
	{
		return baseAssignedFleetProxy.getOwner();
	}

	public String getFleetName()
	{
		return baseAssignedFleetProxy.getFleetName();
	}

	public static <T extends IAssignedFleet> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT AssignedFleet.* FROM AssignedFleet%s%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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


	public static <T extends IAssignedFleet> void insertOrUpdate(SQLiteConnection conn, T assignedFleet) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT celestialBody FROM AssignedFleet WHERE celestialBody = %s AND owner = %s) AS exist ;", "'"+assignedFleet.getCelestialBody()+"'", "'"+assignedFleet.getOwner()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO AssignedFleet (celestialBody, owner, fleetName) VALUES (%s, %s, %s);", "'"+assignedFleet.getCelestialBody()+"'", "'"+assignedFleet.getOwner()+"'", "'"+assignedFleet.getFleetName()+"'"));
			}
			else
			{
				conn.exec(String.format("UPDATE AssignedFleet SET  fleetName = %s WHERE  celestialBody = %s AND owner = %s ;", "'"+assignedFleet.getFleetName()+"'", "'"+assignedFleet.getCelestialBody()+"'", "'"+assignedFleet.getOwner()+"'"));
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
