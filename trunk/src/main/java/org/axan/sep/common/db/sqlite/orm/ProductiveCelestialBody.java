package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.CelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseProductiveCelestialBody;
import org.axan.sep.common.db.IProductiveCelestialBody;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.HashSet;
import java.util.Set;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IAsteroidField;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.INebula;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.IVersionedAsteroidField;
import org.axan.sep.common.db.IVersionedNebula;
import org.axan.sep.common.db.IVersionedPlanet;
import org.axan.sep.common.db.IVersionedProductiveCelestialBody;

public class ProductiveCelestialBody extends CelestialBody implements IProductiveCelestialBody
{
	private final BaseProductiveCelestialBody baseProductiveCelestialBodyProxy;

	public ProductiveCelestialBody(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots)
	{
		super(name, type, location);
		baseProductiveCelestialBodyProxy = new BaseProductiveCelestialBody(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots);
	}

	public ProductiveCelestialBody(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseProductiveCelestialBodyProxy = new BaseProductiveCelestialBody(stmnt);
	}

	public Integer getInitialCarbonStock()
	{
		return baseProductiveCelestialBodyProxy.getInitialCarbonStock();
	}

	public Integer getMaxSlots()
	{
		return baseProductiveCelestialBodyProxy.getMaxSlots();
	}

	/** Set maxVersion to null to select last version. */
	public static <T extends IProductiveCelestialBody> Set<T> selectMaxVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IProductiveCelestialBody> Set<T> selectVersion(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, version, from, where, params);
	}

	public static <T extends IProductiveCelestialBody> Set<T> selectUnversioned(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, String from, String where, Object ... params) throws SQLiteDBException
	{
		return select(conn, config, expectedType, false, null, from, where, params);
	}

	private static <T extends IProductiveCelestialBody> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLiteDBException
	{
		try
		{
			Set<T> results = new HashSet<T>();

			if (where != null && params != null) where = String.format(where, params);
			String versionFilter;
			if (maxVersion)
			{
				versionFilter = String.format("(VersionedProductiveCelestialBody.turn = ( SELECT MAX(LVVersionedProductiveCelestialBody.turn) FROM VersionedProductiveCelestialBody LVVersionedProductiveCelestialBody WHERE LVVersionedProductiveCelestialBody.name = ProductiveCelestialBody.name AND LVVersionedProductiveCelestialBody.type = ProductiveCelestialBody.type%s ))", (version != null && version >= 0) ? " AND LVVersionedProductiveCelestialBody.turn <= "+version : "");
			}
			else
			{
				versionFilter = (version == null) ? "" : String.format("(VersionedProductiveCelestialBody.turn = %d)", version);
			}
			where = String.format("%s%s", (where != null && !where.isEmpty()) ? "("+where+") AND " : "", versionFilter);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT ProductiveCelestialBody.type, VersionedProductiveCelestialBody.type, ProductiveCelestialBody.*, VersionedProductiveCelestialBody.*, VersionedAsteroidField.*, VersionedNebula.*, VersionedPlanet.*, AsteroidField.*, Nebula.*, Planet.* FROM ProductiveCelestialBody%s LEFT JOIN VersionedProductiveCelestialBody USING (name, type) LEFT JOIN VersionedAsteroidField USING (name, turn, type) LEFT JOIN VersionedNebula USING (name, turn, type) LEFT JOIN VersionedPlanet USING (name, turn, type) LEFT JOIN AsteroidField USING (name, type) LEFT JOIN Nebula USING (name, type) LEFT JOIN Planet USING (name, type)%s ;", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : ""));
			while(stmnt.step())
			{
				eCelestialBodyType type = eCelestialBodyType.valueOf(stmnt.columnString(0));
				String v = stmnt.columnString(1);
				SUIS LA: SI v == null celà signifie qu'un celestial body à été inséré dans la DB sans aucune version. Il est bon de crasher dans ce cas ? Reprendre la générale du SQLiteGameBoard en utilisant les classes ORM pour les requetes.
				boolean isVersioned = (v != null && !v.isEmpty());
				Class<? extends IProductiveCelestialBody> clazz = (Class<? extends IProductiveCelestialBody>)  Class.forName(String.format("%s.%s%s", ProductiveCelestialBody.class.getPackage().getName(), isVersioned ? "Versioned" : "", type.toString()));
				IProductiveCelestialBody o = SQLiteORMGenerator.mapTo(clazz, stmnt, config);
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


	public static <T extends IProductiveCelestialBody> void insertOrUpdate(SQLiteConnection conn, T productiveCelestialBody) throws SQLiteDBException
	{
		try
		{
			IVersionedProductiveCelestialBody vproductiveCelestialBody = (IVersionedProductiveCelestialBody.class.isInstance(productiveCelestialBody) ? IVersionedProductiveCelestialBody.class.cast(productiveCelestialBody) : null);
			SQLiteStatement stmnt = conn.prepare(String.format("SELECT EXISTS ( SELECT name FROM ProductiveCelestialBody WHERE name = %s) AS exist ;", "'"+productiveCelestialBody.getName()+"'"));
			stmnt.step();
			if (stmnt.columnInt(0) == 0)
			{
				conn.exec(String.format("INSERT INTO ProductiveCelestialBody (name, type, initialCarbonStock, maxSlots) VALUES (%s, %s, %s, %s);", "'"+productiveCelestialBody.getName()+"'", "'"+productiveCelestialBody.getType()+"'", "'"+productiveCelestialBody.getInitialCarbonStock()+"'", "'"+productiveCelestialBody.getMaxSlots()+"'"));
				if (vproductiveCelestialBody != null)
				{
					conn.exec(String.format("INSERT INTO VersionedProductiveCelestialBody (name, turn, type, owner, carbonStock, currentCarbon) VALUES (%s, %s, %s, %s, %s, %s);", "'"+vproductiveCelestialBody.getName()+"'", "'"+vproductiveCelestialBody.getTurn()+"'", "'"+vproductiveCelestialBody.getType()+"'", "'"+vproductiveCelestialBody.getOwner()+"'", "'"+vproductiveCelestialBody.getCarbonStock()+"'", "'"+vproductiveCelestialBody.getCurrentCarbon()+"'"));
				}
				switch(productiveCelestialBody.getType())
				{
					case Vortex:
					{
						break;
					}
					case Planet:
					{
						IPlanet planet = IPlanet.class.cast(productiveCelestialBody);
						conn.exec(String.format("INSERT INTO Planet (name, type, populationPerTurn, maxPopulation) VALUES (%s, %s, %s, %s);", "'"+planet.getName()+"'", "'"+planet.getType()+"'", "'"+planet.getPopulationPerTurn()+"'", "'"+planet.getMaxPopulation()+"'"));
						if (vproductiveCelestialBody != null)
						{
							IVersionedPlanet versionedPlanet = IVersionedPlanet.class.cast(productiveCelestialBody);
							conn.exec(String.format("INSERT INTO VersionedPlanet (name, turn, type, currentPopulation) VALUES (%s, %s, %s, %s);", "'"+versionedPlanet.getName()+"'", "'"+versionedPlanet.getTurn()+"'", "'"+versionedPlanet.getType()+"'", "'"+versionedPlanet.getCurrentPopulation()+"'"));
						}
						break;
					}
					case AsteroidField:
					{
						IAsteroidField asteroidField = IAsteroidField.class.cast(productiveCelestialBody);
						conn.exec(String.format("INSERT INTO AsteroidField (name, type) VALUES (%s, %s);", "'"+asteroidField.getName()+"'", "'"+asteroidField.getType()+"'"));
						if (vproductiveCelestialBody != null)
						{
							IVersionedAsteroidField versionedAsteroidField = IVersionedAsteroidField.class.cast(productiveCelestialBody);
							conn.exec(String.format("INSERT INTO VersionedAsteroidField (name, turn, type) VALUES (%s, %s, %s);", "'"+versionedAsteroidField.getName()+"'", "'"+versionedAsteroidField.getTurn()+"'", "'"+versionedAsteroidField.getType()+"'"));
						}
						break;
					}
					case Nebula:
					{
						INebula nebula = INebula.class.cast(productiveCelestialBody);
						conn.exec(String.format("INSERT INTO Nebula (name, type) VALUES (%s, %s);", "'"+nebula.getName()+"'", "'"+nebula.getType()+"'"));
						if (vproductiveCelestialBody != null)
						{
							IVersionedNebula versionedNebula = IVersionedNebula.class.cast(productiveCelestialBody);
							conn.exec(String.format("INSERT INTO VersionedNebula (name, turn, type) VALUES (%s, %s, %s);", "'"+versionedNebula.getName()+"'", "'"+versionedNebula.getTurn()+"'", "'"+versionedNebula.getType()+"'"));
						}
						break;
					}
				}
			}
			else
			{
				conn.exec(String.format("UPDATE ProductiveCelestialBody SET  type = %s,  initialCarbonStock = %s,  maxSlots = %s WHERE  name = %s ;", "'"+productiveCelestialBody.getType()+"'", "'"+productiveCelestialBody.getInitialCarbonStock()+"'", "'"+productiveCelestialBody.getMaxSlots()+"'", "'"+productiveCelestialBody.getName()+"'"));
				if (vproductiveCelestialBody != null)
				{
					conn.exec(String.format("UPDATE VersionedProductiveCelestialBody SET  type = %s,  owner = %s,  carbonStock = %s,  currentCarbon = %s WHERE  name = %s AND turn = %s ;", "'"+vproductiveCelestialBody.getType()+"'", "'"+vproductiveCelestialBody.getOwner()+"'", "'"+vproductiveCelestialBody.getCarbonStock()+"'", "'"+vproductiveCelestialBody.getCurrentCarbon()+"'", "'"+vproductiveCelestialBody.getName()+"'", "'"+vproductiveCelestialBody.getTurn()+"'"));
				}
				switch(productiveCelestialBody.getType())
				{
					case Vortex:
					{
						break;
					}
					case Planet:
					{
						IPlanet planet = IPlanet.class.cast(productiveCelestialBody);
						conn.exec(String.format("UPDATE Planet SET  type = %s,  populationPerTurn = %s,  maxPopulation = %s WHERE  name = %s ;", "'"+planet.getType()+"'", "'"+planet.getPopulationPerTurn()+"'", "'"+planet.getMaxPopulation()+"'", "'"+planet.getName()+"'"));
						if (vproductiveCelestialBody != null)
						{
							IVersionedPlanet versionedPlanet = IVersionedPlanet.class.cast(productiveCelestialBody);
							conn.exec(String.format("UPDATE VersionedPlanet SET  type = %s,  currentPopulation = %s WHERE  name = %s AND turn = %s ;", "'"+versionedPlanet.getType()+"'", "'"+versionedPlanet.getCurrentPopulation()+"'", "'"+versionedPlanet.getName()+"'", "'"+versionedPlanet.getTurn()+"'"));
						}
						break;
					}
					case AsteroidField:
					{
						IAsteroidField asteroidField = IAsteroidField.class.cast(productiveCelestialBody);
						conn.exec(String.format("UPDATE AsteroidField SET  type = %s WHERE  name = %s ;", "'"+asteroidField.getType()+"'", "'"+asteroidField.getName()+"'"));
						if (vproductiveCelestialBody != null)
						{
							IVersionedAsteroidField versionedAsteroidField = IVersionedAsteroidField.class.cast(productiveCelestialBody);
							conn.exec(String.format("UPDATE VersionedAsteroidField SET  type = %s WHERE  name = %s AND turn = %s ;", "'"+versionedAsteroidField.getType()+"'", "'"+versionedAsteroidField.getName()+"'", "'"+versionedAsteroidField.getTurn()+"'"));
						}
						break;
					}
					case Nebula:
					{
						INebula nebula = INebula.class.cast(productiveCelestialBody);
						conn.exec(String.format("UPDATE Nebula SET  type = %s WHERE  name = %s ;", "'"+nebula.getType()+"'", "'"+nebula.getName()+"'"));
						if (vproductiveCelestialBody != null)
						{
							IVersionedNebula versionedNebula = IVersionedNebula.class.cast(productiveCelestialBody);
							conn.exec(String.format("UPDATE VersionedNebula SET  type = %s WHERE  name = %s AND turn = %s ;", "'"+versionedNebula.getType()+"'", "'"+versionedNebula.getName()+"'", "'"+versionedNebula.getTurn()+"'"));
						}
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
