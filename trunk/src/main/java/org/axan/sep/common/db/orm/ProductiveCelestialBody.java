package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.CelestialBody;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseProductiveCelestialBody;
import org.axan.sep.common.db.orm.base.BaseProductiveCelestialBody;
import org.axan.sep.common.db.IProductiveCelestialBody;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
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
import org.axan.sep.common.db.SEPCommonDB;

public class ProductiveCelestialBody extends CelestialBody implements IProductiveCelestialBody
{
	private final IBaseProductiveCelestialBody baseProductiveCelestialBodyProxy;

	ProductiveCelestialBody(IBaseProductiveCelestialBody baseProductiveCelestialBodyProxy)
	{
		super(baseProductiveCelestialBodyProxy);
		this.baseProductiveCelestialBodyProxy = baseProductiveCelestialBodyProxy;
	}

	public ProductiveCelestialBody(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots)
	{
		this(new BaseProductiveCelestialBody(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots));
	}

	public ProductiveCelestialBody(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseProductiveCelestialBody(stmnt));
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
	public static <T extends IProductiveCelestialBody> Set<T> selectMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IProductiveCelestialBody> Set<T> selectVersion(SEPCommonDB db, Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IProductiveCelestialBody> Set<T> selectUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return select(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IProductiveCelestialBody> Set<T> select(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		ISQLDataBaseStatement stmnt = null;
		try
		{
			Set<T> results = new HashSet<T>();
			stmnt = db.getDB().prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+";", new ISQLDataBaseStatementJob<ISQLDataBaseStatement>()
			{
				@Override
				public ISQLDataBaseStatement job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
				{
					return stmnt;
				}
			});
			while(stmnt.step())
			{
				eCelestialBodyType type;
				try
				{
					type = eCelestialBodyType.valueOf(stmnt.columnString(0));
				}
				catch(Throwable t)
				{
					throw new Error(t);
				}
				String v = stmnt.columnString(1);
				if (v == null) throw new Error("ProductiveCelestialBody with no VersionedProductiveCelestialBody !");
				boolean isVersioned = (!v.isEmpty());
				Class<? extends IProductiveCelestialBody> clazz = (Class<? extends IProductiveCelestialBody>)  Class.forName(String.format("%s.%s%s", ProductiveCelestialBody.class.getPackage().getName(), isVersioned ? "Versioned" : "", type.toString()));
				IProductiveCelestialBody o = DataBaseORMGenerator.mapTo(clazz, stmnt);
				if (expectedType.isInstance(o))
				{
					results.add(expectedType.cast(o));
				}
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

	/** Set maxVersion to null to select last version. */
	public static <T extends IProductiveCelestialBody> boolean existMaxVersion(SEPCommonDB db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, true, maxVersion, from, where, params);
	}

	public static <T extends IProductiveCelestialBody> boolean existVersion(SEPCommonDB db,Class<T> expectedType, int version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, version, from, where, params);
	}

	public static <T extends IProductiveCelestialBody> boolean existUnversioned(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		return exist(db, expectedType, false, null, from, where, params);
	}

	private static <T extends IProductiveCelestialBody> boolean exist(SEPCommonDB db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws SQLDataBaseException
	{
		try
		{
			return db.getDB().prepare(selectQuery(expectedType, maxVersion, version, from, where, params)+" ;", new ISQLDataBaseStatementJob<Boolean>()
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


	private static <T extends IProductiveCelestialBody> String selectQuery(Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params)
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
		String versionFilter;
		if (maxVersion)
		{
			versionFilter = String.format("(VersionedProductiveCelestialBody.turn = ( SELECT MAX(LVVersionedProductiveCelestialBody.turn) FROM VersionedProductiveCelestialBody LVVersionedProductiveCelestialBody WHERE LVVersionedProductiveCelestialBody.name = ProductiveCelestialBody.name AND LVVersionedProductiveCelestialBody.type = ProductiveCelestialBody.type%s ))", (version != null && version >= 0) ? " AND LVVersionedProductiveCelestialBody.turn <= "+version : "");
		}
		else
		{
			versionFilter = (version == null) ? "" : String.format("(VersionedProductiveCelestialBody.turn = %d)", version);
		}
		if (versionFilter != null && !versionFilter.isEmpty()) where = (where == null) ? versionFilter : String.format("%s AND %s", where, versionFilter);
		return String.format("SELECT ProductiveCelestialBody.type, VersionedProductiveCelestialBody.type, CelestialBody.*, Vortex.*, VersionedProductiveCelestialBody.*, AsteroidField.*, VersionedAsteroidField.*, Nebula.*, VersionedNebula.*, Planet.*, VersionedPlanet.*, ProductiveCelestialBody.* FROM ProductiveCelestialBody%s LEFT JOIN CelestialBody USING (name, type) LEFT JOIN Vortex USING (name, type) LEFT JOIN VersionedProductiveCelestialBody USING (name, type) LEFT JOIN AsteroidField USING (name, type) LEFT JOIN VersionedAsteroidField USING (name, turn, type) LEFT JOIN Nebula USING (name, type) LEFT JOIN VersionedNebula USING (name, turn, type) LEFT JOIN Planet USING (name, type) LEFT JOIN VersionedPlanet USING (name, turn, type)%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IProductiveCelestialBody> void insertOrUpdate(SEPCommonDB db, T productiveCelestialBody) throws SQLDataBaseException
	{
		try
		{
			boolean exist = existUnversioned(db, productiveCelestialBody.getClass(), null, " ProductiveCelestialBody.name = %s", "'"+productiveCelestialBody.getName()+"'");
			IVersionedProductiveCelestialBody vproductiveCelestialBody = (IVersionedProductiveCelestialBody.class.isInstance(productiveCelestialBody) ? IVersionedProductiveCelestialBody.class.cast(productiveCelestialBody) : null);
			if (vproductiveCelestialBody.getOwner() != null && vproductiveCelestialBody.getOwner().length() == 1)
			{
				throw new Error();
			}
			boolean vexist = existVersion(db, vproductiveCelestialBody.getClass(), vproductiveCelestialBody.getTurn(), null, " VersionedProductiveCelestialBody.name = %s AND VersionedProductiveCelestialBody.turn = %s", "'"+vproductiveCelestialBody.getName()+"'", "'"+vproductiveCelestialBody.getTurn()+"'");
			if (vexist && !exist) throw new Error("Versioned ProductiveCelestialBody cannot exist without unversioned entry.");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE CelestialBody SET type = %s,  location_x = %s,  location_y = %s,  location_z = %s WHERE  CelestialBody.name = %s ;", "'"+productiveCelestialBody.getType()+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().x+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().y+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().z+"'", "'"+productiveCelestialBody.getName()+"'").replaceAll("'null'", "NULL"));
				db.getDB().exec(String.format("UPDATE ProductiveCelestialBody SET type = %s,  initialCarbonStock = %s,  maxSlots = %s WHERE  ProductiveCelestialBody.name = %s ;", "'"+productiveCelestialBody.getType()+"'", "'"+productiveCelestialBody.getInitialCarbonStock()+"'", "'"+productiveCelestialBody.getMaxSlots()+"'", "'"+productiveCelestialBody.getName()+"'").replaceAll("'null'", "NULL"));
				if (vexist && vproductiveCelestialBody != null)
				{
					db.getDB().exec(String.format("UPDATE VersionedProductiveCelestialBody SET type = %s,  owner = %s,  carbonStock = %s,  currentCarbon = %s WHERE  VersionedProductiveCelestialBody.name = %s AND VersionedProductiveCelestialBody.turn = %s ;", "'"+vproductiveCelestialBody.getType()+"'", "'"+vproductiveCelestialBody.getOwner()+"'", "'"+vproductiveCelestialBody.getCarbonStock()+"'", "'"+vproductiveCelestialBody.getCurrentCarbon()+"'", "'"+vproductiveCelestialBody.getName()+"'", "'"+vproductiveCelestialBody.getTurn()+"'").replaceAll("'null'", "NULL"));
				}
				switch(productiveCelestialBody.getType())
				{
					case Planet:
					{
						IPlanet planet = IPlanet.class.cast(productiveCelestialBody);
						db.getDB().exec(String.format("UPDATE Planet SET type = %s,  populationPerTurn = %s,  maxPopulation = %s WHERE  Planet.name = %s ;", "'"+planet.getType()+"'", "'"+planet.getPopulationPerTurn()+"'", "'"+planet.getMaxPopulation()+"'", "'"+planet.getName()+"'").replaceAll("'null'", "NULL"));
						if (vexist && vproductiveCelestialBody != null)
						{
							IVersionedPlanet versionedPlanet = IVersionedPlanet.class.cast(productiveCelestialBody);
							db.getDB().exec(String.format("UPDATE VersionedPlanet SET type = %s,  currentPopulation = %s WHERE  VersionedPlanet.name = %s AND VersionedPlanet.turn = %s ;", "'"+versionedPlanet.getType()+"'", "'"+versionedPlanet.getCurrentPopulation()+"'", "'"+versionedPlanet.getName()+"'", "'"+versionedPlanet.getTurn()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case AsteroidField:
					{
						IAsteroidField asteroidField = IAsteroidField.class.cast(productiveCelestialBody);
						db.getDB().exec(String.format("UPDATE AsteroidField SET type = %s WHERE  AsteroidField.name = %s ;", "'"+asteroidField.getType()+"'", "'"+asteroidField.getName()+"'").replaceAll("'null'", "NULL"));
						if (vexist && vproductiveCelestialBody != null)
						{
							IVersionedAsteroidField versionedAsteroidField = IVersionedAsteroidField.class.cast(productiveCelestialBody);
							db.getDB().exec(String.format("UPDATE VersionedAsteroidField SET type = %s WHERE  VersionedAsteroidField.name = %s AND VersionedAsteroidField.turn = %s ;", "'"+versionedAsteroidField.getType()+"'", "'"+versionedAsteroidField.getName()+"'", "'"+versionedAsteroidField.getTurn()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case Nebula:
					{
						INebula nebula = INebula.class.cast(productiveCelestialBody);
						db.getDB().exec(String.format("UPDATE Nebula SET type = %s WHERE  Nebula.name = %s ;", "'"+nebula.getType()+"'", "'"+nebula.getName()+"'").replaceAll("'null'", "NULL"));
						if (vexist && vproductiveCelestialBody != null)
						{
							IVersionedNebula versionedNebula = IVersionedNebula.class.cast(productiveCelestialBody);
							db.getDB().exec(String.format("UPDATE VersionedNebula SET type = %s WHERE  VersionedNebula.name = %s AND VersionedNebula.turn = %s ;", "'"+versionedNebula.getType()+"'", "'"+versionedNebula.getName()+"'", "'"+versionedNebula.getTurn()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
				}
			}
			if (!exist || !vexist)
			{
				try
				{
					if (!exist) db.getDB().exec(String.format("INSERT INTO CelestialBody (name, type, location_x, location_y, location_z) VALUES (%s, %s, %s, %s, %s);", "'"+productiveCelestialBody.getName()+"'", "'"+productiveCelestialBody.getType()+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().x+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().y+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().z+"'").replaceAll("'null'", "NULL"));
				}
				catch(Throwable t)
				{
					throw new Error(t);
				}
				if (!exist) db.getDB().exec(String.format("INSERT INTO ProductiveCelestialBody (name, type, initialCarbonStock, maxSlots) VALUES (%s, %s, %s, %s);", "'"+productiveCelestialBody.getName()+"'", "'"+productiveCelestialBody.getType()+"'", "'"+productiveCelestialBody.getInitialCarbonStock()+"'", "'"+productiveCelestialBody.getMaxSlots()+"'").replaceAll("'null'", "NULL"));
				if (vproductiveCelestialBody != null)
				{
					try
					{
						db.getDB().exec(String.format("INSERT INTO VersionedProductiveCelestialBody (name, turn, type, owner, carbonStock, currentCarbon) VALUES (%s, %s, %s, %s, %s, %s);", "'"+vproductiveCelestialBody.getName()+"'", "'"+vproductiveCelestialBody.getTurn()+"'", "'"+vproductiveCelestialBody.getType()+"'", "'"+vproductiveCelestialBody.getOwner()+"'", "'"+vproductiveCelestialBody.getCarbonStock()+"'", "'"+vproductiveCelestialBody.getCurrentCarbon()+"'").replaceAll("'null'", "NULL"));
					}
					catch(Throwable t)
					{
						throw new Error(t);
					}
				}
				switch(productiveCelestialBody.getType())
				{
					case Planet:
					{
						IPlanet planet = IPlanet.class.cast(productiveCelestialBody);
						if (!exist) db.getDB().exec(String.format("INSERT INTO Planet (name, type, populationPerTurn, maxPopulation) VALUES (%s, %s, %s, %s);", "'"+planet.getName()+"'", "'"+planet.getType()+"'", "'"+planet.getPopulationPerTurn()+"'", "'"+planet.getMaxPopulation()+"'").replaceAll("'null'", "NULL"));
						if (vproductiveCelestialBody != null)
						{
							IVersionedPlanet versionedPlanet = IVersionedPlanet.class.cast(productiveCelestialBody);
							db.getDB().exec(String.format("INSERT INTO VersionedPlanet (name, turn, type, currentPopulation) VALUES (%s, %s, %s, %s);", "'"+versionedPlanet.getName()+"'", "'"+versionedPlanet.getTurn()+"'", "'"+versionedPlanet.getType()+"'", "'"+versionedPlanet.getCurrentPopulation()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case AsteroidField:
					{
						IAsteroidField asteroidField = IAsteroidField.class.cast(productiveCelestialBody);
						if (!exist) db.getDB().exec(String.format("INSERT INTO AsteroidField (name, type) VALUES (%s, %s);", "'"+asteroidField.getName()+"'", "'"+asteroidField.getType()+"'").replaceAll("'null'", "NULL"));
						if (vproductiveCelestialBody != null)
						{
							IVersionedAsteroidField versionedAsteroidField = IVersionedAsteroidField.class.cast(productiveCelestialBody);
							db.getDB().exec(String.format("INSERT INTO VersionedAsteroidField (name, turn, type) VALUES (%s, %s, %s);", "'"+versionedAsteroidField.getName()+"'", "'"+versionedAsteroidField.getTurn()+"'", "'"+versionedAsteroidField.getType()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
					case Nebula:
					{
						INebula nebula = INebula.class.cast(productiveCelestialBody);
						if (!exist) db.getDB().exec(String.format("INSERT INTO Nebula (name, type) VALUES (%s, %s);", "'"+nebula.getName()+"'", "'"+nebula.getType()+"'").replaceAll("'null'", "NULL"));
						if (vproductiveCelestialBody != null)
						{
							IVersionedNebula versionedNebula = IVersionedNebula.class.cast(productiveCelestialBody);
							db.getDB().exec(String.format("INSERT INTO VersionedNebula (name, turn, type) VALUES (%s, %s, %s);", "'"+versionedNebula.getName()+"'", "'"+versionedNebula.getTurn()+"'", "'"+versionedNebula.getType()+"'").replaceAll("'null'", "NULL"));
						}
						break;
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new SQLDataBaseException(e);
		}
	}
}
