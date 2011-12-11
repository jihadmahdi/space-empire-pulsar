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
import org.axan.sep.common.db.IAsteroidField;
import org.axan.sep.common.db.INebula;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BaseProductiveCelestialBody;
import org.axan.sep.common.db.orm.base.IBaseProductiveCelestialBody;

public class ProductiveCelestialBody extends CelestialBody implements IProductiveCelestialBody, Serializable
{
	private final IBaseProductiveCelestialBody baseProductiveCelestialBodyProxy;

	ProductiveCelestialBody(IBaseProductiveCelestialBody baseProductiveCelestialBodyProxy)
	{
		super(baseProductiveCelestialBodyProxy);
		this.baseProductiveCelestialBodyProxy = baseProductiveCelestialBodyProxy;
	}

	public ProductiveCelestialBody(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, String owner, Integer carbonStock, Integer currentCarbon)
	{
		this(new BaseProductiveCelestialBody(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, owner, carbonStock, currentCarbon));
	}

	public ProductiveCelestialBody(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseProductiveCelestialBody(stmnt));
	}

	@Override
	public Integer getInitialCarbonStock()
	{
		return baseProductiveCelestialBodyProxy.getInitialCarbonStock();
	}

	@Override
	public Integer getMaxSlots()
	{
		return baseProductiveCelestialBodyProxy.getMaxSlots();
	}

	@Override
	public String getOwner()
	{
		return baseProductiveCelestialBodyProxy.getOwner();
	}

	@Override
	public Integer getCarbonStock()
	{
		return baseProductiveCelestialBodyProxy.getCarbonStock();
	}

	@Override
	public Integer getCurrentCarbon()
	{
		return baseProductiveCelestialBodyProxy.getCurrentCarbon();
	}

	public static <T extends IProductiveCelestialBody> T selectOne(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
	{
		Set<T> results = select(db, expectedType, from, (where==null?"(1) ":"("+where+") ")+"LIMIT 1", params);
		if (results.isEmpty()) return null;
		return results.iterator().next();
	}

	public static <T extends IProductiveCelestialBody> Set<T> select(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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
				results.add(DataBaseORMGenerator.mapTo(expectedType, stmnt));
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

	public static <T extends IProductiveCelestialBody> boolean exist(SEPCommonDB db, Class<T> expectedType, String from, String where, Object ... params) throws SQLDataBaseException
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


	private static <T extends IProductiveCelestialBody> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)
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
		return String.format("SELECT CelestialBody.*, Vortex.*, ProductiveCelestialBody.*, AsteroidField.*, Nebula.*, Planet.* FROM ProductiveCelestialBody%s LEFT JOIN CelestialBody USING (name, type) LEFT JOIN Vortex USING (name, type) LEFT JOIN AsteroidField USING (name, type) LEFT JOIN Nebula USING (name, type) LEFT JOIN Planet USING (name, type)%s", (from != null && !from.isEmpty()) ? ", "+from : "", (where != null && !where.isEmpty()) ? " WHERE "+where : "");
	}

	public static <T extends IProductiveCelestialBody> void insertOrUpdate(SEPCommonDB db, T productiveCelestialBody) throws SQLDataBaseException
	{
		try
		{
			boolean exist = exist(db, productiveCelestialBody.getClass(), null, " ProductiveCelestialBody.name = %s", "'"+productiveCelestialBody.getName()+"'");
			if (exist)
			{
				db.getDB().exec(String.format("UPDATE CelestialBody SET type = %s,  location_x = %s,  location_y = %s,  location_z = %s WHERE  CelestialBody.name = %s ;", "'"+productiveCelestialBody.getType()+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().x+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().y+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().z+"'", "'"+productiveCelestialBody.getName()+"'").replaceAll("'null'", "NULL"));
				db.getDB().exec(String.format("UPDATE ProductiveCelestialBody SET type = %s,  initialCarbonStock = %s,  maxSlots = %s,  owner = %s,  carbonStock = %s,  currentCarbon = %s WHERE  ProductiveCelestialBody.name = %s ;", "'"+productiveCelestialBody.getType()+"'", "'"+productiveCelestialBody.getInitialCarbonStock()+"'", "'"+productiveCelestialBody.getMaxSlots()+"'", "'"+productiveCelestialBody.getOwner()+"'", "'"+productiveCelestialBody.getCarbonStock()+"'", "'"+productiveCelestialBody.getCurrentCarbon()+"'", "'"+productiveCelestialBody.getName()+"'").replaceAll("'null'", "NULL"));
				switch(productiveCelestialBody.getType())
				{
					case Planet:
					{
						IPlanet planet = IPlanet.class.cast(productiveCelestialBody);
						db.getDB().exec(String.format("UPDATE Planet SET type = %s,  populationPerTurn = %s,  maxPopulation = %s,  currentPopulation = %s WHERE  Planet.name = %s ;", "'"+planet.getType()+"'", "'"+planet.getPopulationPerTurn()+"'", "'"+planet.getMaxPopulation()+"'", "'"+planet.getCurrentPopulation()+"'", "'"+planet.getName()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case AsteroidField:
					{
						IAsteroidField asteroidField = IAsteroidField.class.cast(productiveCelestialBody);
						db.getDB().exec(String.format("UPDATE AsteroidField SET type = %s WHERE  AsteroidField.name = %s ;", "'"+asteroidField.getType()+"'", "'"+asteroidField.getName()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case Nebula:
					{
						INebula nebula = INebula.class.cast(productiveCelestialBody);
						db.getDB().exec(String.format("UPDATE Nebula SET type = %s WHERE  Nebula.name = %s ;", "'"+nebula.getType()+"'", "'"+nebula.getName()+"'").replaceAll("'null'", "NULL"));
						break;
					}
				}
			}
			else
			{
				if (!exist) db.getDB().exec(String.format("INSERT INTO CelestialBody (name, type, location_x, location_y, location_z) VALUES (%s, %s, %s, %s, %s);", "'"+productiveCelestialBody.getName()+"'", "'"+productiveCelestialBody.getType()+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().x+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().y+"'", productiveCelestialBody.getLocation() == null ? "NULL" : "'"+productiveCelestialBody.getLocation().z+"'").replaceAll("'null'", "NULL"));
				if (!exist) db.getDB().exec(String.format("INSERT INTO ProductiveCelestialBody (name, type, initialCarbonStock, maxSlots, owner, carbonStock, currentCarbon) VALUES (%s, %s, %s, %s, %s, %s, %s);", "'"+productiveCelestialBody.getName()+"'", "'"+productiveCelestialBody.getType()+"'", "'"+productiveCelestialBody.getInitialCarbonStock()+"'", "'"+productiveCelestialBody.getMaxSlots()+"'", "'"+productiveCelestialBody.getOwner()+"'", "'"+productiveCelestialBody.getCarbonStock()+"'", "'"+productiveCelestialBody.getCurrentCarbon()+"'").replaceAll("'null'", "NULL"));
				switch(productiveCelestialBody.getType())
				{
					case Planet:
					{
						IPlanet planet = IPlanet.class.cast(productiveCelestialBody);
						if (!exist) db.getDB().exec(String.format("INSERT INTO Planet (name, type, populationPerTurn, maxPopulation, currentPopulation) VALUES (%s, %s, %s, %s, %s);", "'"+planet.getName()+"'", "'"+planet.getType()+"'", "'"+planet.getPopulationPerTurn()+"'", "'"+planet.getMaxPopulation()+"'", "'"+planet.getCurrentPopulation()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case AsteroidField:
					{
						IAsteroidField asteroidField = IAsteroidField.class.cast(productiveCelestialBody);
						if (!exist) db.getDB().exec(String.format("INSERT INTO AsteroidField (name, type) VALUES (%s, %s);", "'"+asteroidField.getName()+"'", "'"+asteroidField.getType()+"'").replaceAll("'null'", "NULL"));
						break;
					}
					case Nebula:
					{
						INebula nebula = INebula.class.cast(productiveCelestialBody);
						if (!exist) db.getDB().exec(String.format("INSERT INTO Nebula (name, type) VALUES (%s, %s);", "'"+nebula.getName()+"'", "'"+nebula.getType()+"'").replaceAll("'null'", "NULL"));
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
