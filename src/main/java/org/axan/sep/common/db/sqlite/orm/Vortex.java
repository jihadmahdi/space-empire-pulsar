package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.CelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseVortex;
import org.axan.sep.common.db.IVortex;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IAsteroidField;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.INebula;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.IVortex;

public class Vortex extends CelestialBody implements IVortex
{
	private final BaseVortex baseVortexProxy;

	public Vortex(String name, eCelestialBodyType type, Location location, Integer onsetDate, Integer endDate, String destination)
	{
		super(name, type, location);
		baseVortexProxy = new BaseVortex(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, onsetDate, endDate, destination);
	}

	public Vortex(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseVortexProxy = new BaseVortex(stmnt);
	}

	public Integer getOnsetDate()
	{
		return baseVortexProxy.getOnsetDate();
	}

	public Integer getEndDate()
	{
		return baseVortexProxy.getEndDate();
	}

	public String getDestination()
	{
		return baseVortexProxy.getDestination();
	}

	public static <T extends IVortex> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();
			SQLiteStatement stmnt = conn.prepare(selectQuery(expectedType, from, where, params)+";");
			while(stmnt.step())
			{
				results.add(SQLiteORMGenerator.mapTo(expectedType.isInterface() ? (Class<T>) Vortex.class : expectedType, stmnt, config));
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}

	public static <T extends IVortex> boolean exist(SQLiteConnection conn, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare("SELECT EXISTS ( "+selectQuery(expectedType, from, where, params) + " );");
			return stmnt.step() && stmnt.columnInt(0) != 0;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}


	private static <T extends IVortex> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
	{
		where = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);
		if (where != null) where = String.format("(%s)",where);
		String typeFilter = null;
		if (expectedType != null)
		{
			String type = expectedType.isInterface() ? expectedType.getSimpleName().substring(1) : expectedType.getSimpleName();
			typeFilter = String.format("%s.type IS NOT NULL", type);
		}
		if (typeFilter != null && !typeFilter.isEmpty()) where = (where == null) ? typeFilter : String.format("%s AND %s", where, typeFilter);
		return String.format("SELECT CelestialBody.*, VersionedProductiveCelestialBody.*, AsteroidField.*, VersionedAsteroidField.*, Nebula.*, VersionedNebula.*, Planet.*, VersionedPlanet.*, ProductiveCelestialBody.*, Vortex.* FROM Vortex%s LEFT JOIN CelestialBody USING (name, type) LEFT JOIN VersionedProductiveCelestialBody USING (name, type) LEFT JOIN AsteroidField USING (name, type) LEFT JOIN VersionedAsteroidField USING (name, turn, type) LEFT JOIN Nebula USING (name, type) LEFT JOIN VersionedNebula USING (name, turn, type) LEFT JOIN Planet USING (name, type) LEFT JOIN VersionedPlanet USING (name, turn, type) LEFT JOIN ProductiveCelestialBody USING (name, type)%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IVortex> void insertOrUpdate(SQLiteConnection conn, T vortex) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT name FROM Vortex WHERE name = %s) AS exist ;", "'"+vortex.getName()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO CelestialBody (name, type, location_x, location_y, location_z) VALUES (%s, %s, %s, %s, %s);", "'"+vortex.getName()+"'", "'"+vortex.getType()+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().x+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().y+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().z+"'").replaceAll("'null'", "NULL"));
				conn.exec(String.format("INSERT INTO Vortex (name, type, onsetDate, endDate, destination) VALUES (%s, %s, %s, %s, %s);", "'"+vortex.getName()+"'", "'"+vortex.getType()+"'", "'"+vortex.getOnsetDate()+"'", "'"+vortex.getEndDate()+"'", "'"+vortex.getDestination()+"'").replaceAll("'null'", "NULL"));
				switch(vortex.getType())
				{
				}
			}
			else
			{
				conn.exec(String.format("UPDATE CelestialBody SET type = %s,  location_x = %s,  location_y = %s,  location_z = %s WHERE  name = %s ;", "'"+vortex.getType()+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().x+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().y+"'", vortex.getLocation() == null ? "NULL" : "'"+vortex.getLocation().z+"'", "'"+vortex.getName()+"'").replaceAll("'null'", "NULL"));
				conn.exec(String.format("UPDATE Vortex SET type = %s,  onsetDate = %s,  endDate = %s,  destination = %s WHERE  name = %s ;", "'"+vortex.getType()+"'", "'"+vortex.getOnsetDate()+"'", "'"+vortex.getEndDate()+"'", "'"+vortex.getDestination()+"'", "'"+vortex.getName()+"'").replaceAll("'null'", "NULL"));
				switch(vortex.getType())
				{
				}
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
