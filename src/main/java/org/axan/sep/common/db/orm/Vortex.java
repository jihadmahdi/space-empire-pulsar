package org.axan.sep.common.db.orm;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IVortex;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseVortex;
import org.axan.sep.common.db.orm.base.IBaseVortex;

public class Vortex extends CelestialBody implements IVortex, Serializable
{
	private final IBaseVortex baseVortexProxy;

	Vortex(IBaseVortex baseVortexProxy)
	{
		super(baseVortexProxy);
		this.baseVortexProxy = baseVortexProxy;
	}

	public Vortex(String name, eCelestialBodyType type, Location location, Integer onsetDate, Integer endDate, String destination)
	{
		this(new BaseVortex(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, onsetDate, endDate, destination));
	}

	public Vortex(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseVortex(stmnt));
	}

	@Override
	public Integer getOnsetDate()
	{
		return baseVortexProxy.getOnsetDate();
	}

	@Override
	public Integer getEndDate()
	{
		return baseVortexProxy.getEndDate();
	}

	@Override
	public String getDestination()
	{
		return baseVortexProxy.getDestination();
	}

	public static <T extends IVortex> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"(1) ":"("+where+") ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends IVortex> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Vortex.class : expectedType, stmnt));
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

	public static <T extends IVortex> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IVortex> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null && !where.isEmpty() && where.charAt(0) != '(') where = "("+where+")";
		String typeFilter = null;
		if (expectedType != null)
		{
			String type = expectedType.isInterface() ? expectedType.getSimpleName().substring(1) : expectedType.getSimpleName();
			typeFilter = String.format("%s.type IS NOT NULL", type);
		}
		if (typeFilter != null && !typeFilter.isEmpty()) where = (where == null) ? typeFilter : String.format("%s AND %s", typeFilter, where);
		return String.format("SELECT CelestialBody.*, ProductiveCelestialBody.*, AsteroidField.*, Nebula.*, Planet.*, Vortex.* FROM Vortex%s LEFT JOIN CelestialBody USING (name, type) LEFT JOIN ProductiveCelestialBody USING (name, type) LEFT JOIN AsteroidField USING (name, type) LEFT JOIN Nebula USING (name, type) LEFT JOIN Planet USING (name, type)%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IVortex> void insertOrUpdate(SEPCommonDB db, T vortex) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, vortex.getClass(), null, " Vortex.name = %s", "'"+vortex.getName()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE CelestialBody SET type = %s,  location_x = %s,  location_y = %s,  location_z = %s WHERE  CelestialBody.name = %s ;", "'"+vortex.getType()+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().x+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().y+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().z+"'", "'"+vortex.getName()+"'").replaceAll("'null'", "NULL"));
				db.getDB().exec(String.format("UPDATE Vortex SET type = %s,  onsetDate = %s,  endDate = %s,  destination = %s WHERE  Vortex.name = %s ;", "'"+vortex.getType()+"'", "'"+vortex.getOnsetDate()+"'", "'"+vortex.getEndDate()+"'", "'"+vortex.getDestination()+"'", "'"+vortex.getName()+"'").replaceAll("'null'", "NULL"));
				switch(vortex.getType())
				{
				}
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO CelestialBody (name, type, location_x, location_y, location_z) VALUES (%s, %s, %s, %s, %s);", "'"+vortex.getName()+"'", "'"+vortex.getType()+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().x+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().y+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().z+"'").replaceAll("'null'", "NULL"));
				if (!exist) db.getDB().exec(String.format("INSERT INTO Vortex (name, type, onsetDate, endDate, destination) VALUES (%s, %s, %s, %s, %s);", "'"+vortex.getName()+"'", "'"+vortex.getType()+"'", "'"+vortex.getOnsetDate()+"'", "'"+vortex.getEndDate()+"'", "'"+vortex.getDestination()+"'").replaceAll("'null'", "NULL"));
				switch(vortex.getType())
				{
				}
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
