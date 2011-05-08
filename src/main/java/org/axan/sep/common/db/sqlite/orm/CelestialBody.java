package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseCelestialBody;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public class CelestialBody implements ICelestialBody
{
	private final BaseCelestialBody baseCelestialBodyProxy;
	private eCelestialBodyType type;private Location location;

	public CelestialBody(String name, eCelestialBodyType type, Location location)
	{
		baseCelestialBodyProxy = new BaseCelestialBody(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z);
		this.type = type;
		this.location = location;
	}

	public CelestialBody(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseCelestialBodyProxy = new BaseCelestialBody(stmnt);
		this.type = eCelestialBodyType.valueOf(baseCelestialBodyProxy.getType());
		this.location = (baseCelestialBodyProxy.getLocation_x() == null ? null : new Location(baseCelestialBodyProxy.getLocation_x(), baseCelestialBodyProxy.getLocation_y(), baseCelestialBodyProxy.getLocation_z()));
	}

	public String getName()
	{
		return baseCelestialBodyProxy.getName();
	}

	public eCelestialBodyType getType()
	{
		return type;
	}

	public Location getLocation()
	{
		return location;
	}

	public static <T extends ICelestialBody> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT * FROM CelestialBody LEFT JOIN Vortex USING (name, type) LEFT JOIN ProductiveCelestialBody USING (name, type) LEFT JOIN VersionedProductiveCelestialBody USING (name, type) LEFT JOIN VersionedAsteroidField USING (name, turn, type) LEFT JOIN VersionedNebula USING (name, turn, type) LEFT JOIN VersionedPlanet USING (name, turn, type) LEFT JOIN AsteroidField USING (name, type) LEFT JOIN Nebula USING (name, type) LEFT JOIN Planet USING (name, type)%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
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


	public static <T extends ICelestialBody> void insertOrUpdate(SQLiteConnection conn, T celestialBody) throws SQLiteDBException
	{
		try
		{
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT name FROM CelestialBody WHERE name = %s) AS exist ;", "'"+celestialBody.getName()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO CelestialBody (name, type, location_x, location_y, location_z) VALUES (%s, %s, %s, %s, %s);", "'"+celestialBody.getName()+"'", "'"+celestialBody.getType()+"'", celestialBody.getLocation() == null ? "NULL" : "'"+celestialBody.getLocation().x+"'", celestialBody.getLocation() == null ? "NULL" : "'"+celestialBody.getLocation().y+"'", celestialBody.getLocation() == null ? "NULL" : "'"+celestialBody.getLocation().z+"'"));
				switch(celestialBody.getType())
				{
					case Vortex:
					{
						IVortex vortex = IVortex.class.cast(celestialBody);
						conn.exec(String.format("INSERT INTO Vortex (name, type, onsetDate, endDate, destination) VALUES (%s, %s, %s, %s, %s);", "'"+vortex.getName()+"'", "'"+vortex.getType()+"'", "'"+vortex.getOnsetDate()+"'", "'"+vortex.getEndDate()+"'", "'"+vortex.getDestination()+"'"));
						break;
					}
					case Planet:
					{
						break;
					}
					case AsteroidField:
					{
						break;
					}
					case Nebula:
					{
						break;
					}
				}
			}
			else
			{
				conn.exec(String.format("UPDATE CelestialBody SET  type = %s,  location_x = %s,  location_y = %s,  location_z = %s WHERE  name = %s ;", "'"+celestialBody.getType()+"'", celestialBody.getLocation() == null ? "NULL" : "'"+celestialBody.getLocation().x+"'", celestialBody.getLocation() == null ? "NULL" : "'"+celestialBody.getLocation().y+"'", celestialBody.getLocation() == null ? "NULL" : "'"+celestialBody.getLocation().z+"'", "'"+celestialBody.getName()+"'"));
				switch(celestialBody.getType())
				{
					case Vortex:
					{
						IVortex vortex = IVortex.class.cast(celestialBody);
						conn.exec(String.format("UPDATE Vortex SET  type = %s,  onsetDate = %s,  endDate = %s,  destination = %s WHERE  name = %s ;", "'"+vortex.getType()+"'", "'"+vortex.getOnsetDate()+"'", "'"+vortex.getEndDate()+"'", "'"+vortex.getDestination()+"'", "'"+vortex.getName()+"'"));
						break;
					}
					case Planet:
					{
						break;
					}
					case AsteroidField:
					{
						break;
					}
					case Nebula:
					{
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}
}
