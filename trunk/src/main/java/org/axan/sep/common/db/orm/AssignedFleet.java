package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseAssignedFleet;
import org.axan.sep.common.db.orm.base.BaseAssignedFleet;
import org.axan.sep.common.db.IAssignedFleet;
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

public class AssignedFleet implements IAssignedFleet
{
	private final IBaseAssignedFleet baseAssignedFleetProxy;

	AssignedFleet(IBaseAssignedFleet baseAssignedFleetProxy)
	{
		this.baseAssignedFleetProxy = baseAssignedFleetProxy;
	}

	public AssignedFleet(String celestialBody, String owner, String fleetName)
	{
		this(new BaseAssignedFleet(celestialBody, owner, fleetName));
	}

	public AssignedFleet(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseAssignedFleet(stmnt));
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

	public static <T extends IAssignedFleet> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) AssignedFleet.class : expectedType, stmnt));
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

	public static <T extends IAssignedFleet> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IAssignedFleet> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		return String.format("SELECT AssignedFleet.* FROM AssignedFleet%s%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IAssignedFleet> void insertOrUpdate(SEPCommonDB db, T assignedFleet) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, assignedFleet.getClass(), null, " AssignedFleet.celestialBody = %s AND AssignedFleet.owner = %s", "'"+assignedFleet.getCelestialBody()+"'", "'"+assignedFleet.getOwner()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE AssignedFleet SET fleetName = %s WHERE  AssignedFleet.celestialBody = %s AND AssignedFleet.owner = %s ;", "'"+assignedFleet.getFleetName()+"'", "'"+assignedFleet.getCelestialBody()+"'", "'"+assignedFleet.getOwner()+"'").replaceAll("'null'", "NULL"));
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO AssignedFleet (celestialBody, owner, fleetName) VALUES (%s, %s, %s);", "'"+assignedFleet.getCelestialBody()+"'", "'"+assignedFleet.getOwner()+"'", "'"+assignedFleet.getFleetName()+"'").replaceAll("'null'", "NULL"));
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
