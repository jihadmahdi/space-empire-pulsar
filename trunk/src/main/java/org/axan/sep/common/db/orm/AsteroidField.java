package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.ProductiveCelestialBody;
import org.axan.sep.common.db.orm.base.IBaseAsteroidField;
import org.axan.sep.common.db.orm.base.BaseAsteroidField;
import org.axan.sep.common.db.IAsteroidField;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

class AsteroidField extends ProductiveCelestialBody implements IAsteroidField
{
	// PK inherited.
	
	/*
	 * Off-DB: off db fields (none).
	 */
	
	/*
	 * DB connection
	 */
	protected transient Index<Node> asteroidFieldIndex;
	
	/**
	 * Off-DB constructor
	 * @param name
	 * @param initialCarbonStock
	 * @param maxSlots
	 * @param carbonStock
	 * @param currentCarbon
	 */
	public AsteroidField(String name, Location location, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon)
	{
		super(name, location, initialCarbonStock, maxSlots, carbonStock, currentCarbon);
	}

	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param name
	 */
	public AsteroidField(SEPCommonDB sepDB, String name)
	{
		super(sepDB, name);
	}
	
	@Override
	final protected void checkForDBUpdate()
	{				
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			super.checkForDBUpdate();			
			asteroidFieldIndex = db.index().forNodes("AsteroidFieldIndex");			
		}
	}

	/**
	 * Create method final implementation.
	 * Final implement actually create the db node and initialize it.
	 */
	@Override
	final protected void create(SEPCommonDB sepDB)
	{
		assertOnlineStatus(false, "Illegal state: can only call create(SEPCommonDB) method on Off-DB objects.");
		
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			this.sepDB = sepDB;
			checkForDBUpdate();
			
			if (asteroidFieldIndex.get(PK, getPK(name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, asteroidField[name='"+name+"'] already exist.");
			}
			properties = sepDB.getDB().createNode();
			AsteroidField.initializeProperties(properties, name, initialCarbonStock, maxSlots, carbonStock, currentCarbon);
			asteroidFieldIndex.add(properties, PK, getPK(name));
			
			super.create(sepDB);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}

	public static void initializeProperties(Node properties, String name, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon)
	{
		properties.setProperty("name", name);
		properties.setProperty("type", eCelestialBodyType.AsteroidField.toString());
		properties.setProperty("initialCarbonStock", initialCarbonStock);
		properties.setProperty("maxSlots", maxSlots);
		properties.setProperty("carbonStock", carbonStock);
		properties.setProperty("currentCarbon", currentCarbon);
	}

}
