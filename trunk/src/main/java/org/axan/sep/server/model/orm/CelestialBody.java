package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.BaseCelestialBody;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import java.util.HashSet;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteConnection;

public class CelestialBody implements ICelestialBody
{
	private final BaseCelestialBody baseCelestialBodyProxy;

	public CelestialBody(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseCelestialBodyProxy = new BaseCelestialBody(stmnt);
	}

	public Location getLocation()
	{
		return (baseCelestialBodyProxy.getLocation_x() == null) ? null : new Location(baseCelestialBodyProxy.getLocation_x(), baseCelestialBodyProxy.getLocation_y(), baseCelestialBodyProxy.getLocation_z());
	}

	public eCelestialBodyType getType()
	{
		return eCelestialBodyType.valueOf(baseCelestialBodyProxy.getType());
	}

	public String getName()
	{
		return baseCelestialBodyProxy.getName();
	}

	public static <T extends ICelestialBody> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean lastVersion, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT CelestialBody.type, VersionedCelestialBody.type, * FROM CelestialBody LEFT JOIN Vortex USING (type, name) LEFT JOIN ProductiveCelestialBody USING (type, name) LEFT JOIN VersionedProductiveCelestialBody USING (type, name) LEFT JOIN VersionedAsteroidField USING (turn, type, name) LEFT JOIN VersionedNebula USING (turn, type, name) LEFT JOIN VersionedPlanet USING (turn, type, name) LEFT JOIN AsteroidField USING (type, name) LEFT JOIN Nebula USING (type, name) LEFT JOIN Planet USING (type, name)%s ;", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				eCelestialBodyType type = eCelestialBodyType.valueOf(stmnt.columnString(0));
				boolean isVersioned = (!stmnt.columnString(1).isEmpty());
				Class<? extends ICelestialBody> clazz = (Class<? extends ICelestialBody>)  Class.forName(String.format("%s.%s%s", CelestialBody.class.getPackage().getName(), isVersioned ? "Versioned" : "", type.toString()));
				ICelestialBody o = SQLiteORMGenerator.mapTo(clazz, stmnt, config);
				if (expectedType.isInstance(o))
				{
					results.add(expectedType.cast(o));
				}
			}
			return results;
		}
		catch(Exception e)
		{
			throw new SQLiteDBException(e);
		}
	}

}
