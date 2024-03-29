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

import javax.annotation.OverridingMethodsMustInvokeSuper;

class AsteroidField extends ProductiveCelestialBody implements IAsteroidField
{
	// PK inherited.
	
	/*
	 * Off-DB: off db fields (none).
	 */
	
	/*
	 * DB connection
	 */
	
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
		super.checkForDBUpdate();
	}
	
	@Override
	final protected void initializeProperties()
	{
		super.initializeProperties();
	}
	
	/**
	 * Register properties (add Node to indexes and create relationships).
	 * @param properties
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	final protected void register(Node properties)
	{
		super.register(properties);
	}
}
