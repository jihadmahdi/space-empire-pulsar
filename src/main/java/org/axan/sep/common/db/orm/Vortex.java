package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.CelestialBody;
import org.axan.sep.common.db.orm.base.IBaseVortex;
import org.axan.sep.common.db.orm.base.BaseVortex;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IVortex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.axan.sep.common.db.IGameConfig;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

class Vortex extends CelestialBody implements IVortex, Serializable
{
	/*
	 * PK (inherited): first pk field.
	 */
	
	/*
	 * Off-DB fields (none)
	 */
	protected final int birth;
	protected final int death;
	
	/*
	 * DB connection
	 */
	
	/**
	 * Off-DB constructor
	 * @param name
	 * @param birth
	 * @param death
	 */
	public Vortex(String name, Location location, int birth, int death)
	{
		super(name, location);
		this.birth = birth;
		this.death = death;
	}

	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param name
	 */
	public Vortex(SEPCommonDB sepDB, String name)
	{
		super(sepDB, name);
		
		// Null values
		this.birth = 0;
		this.death = 0;
	}

	@Override
	final protected void checkForDBUpdate()
	{				
		super.checkForDBUpdate();
	}
	
	@Override
	protected void initializeProperties()
	{
		super.initializeProperties();
		properties.setProperty("birth", birth);
		properties.setProperty("death", death);
	}
	
	@Override
	final protected void register(Node properties)
	{
		super.register(properties);
	}
	
	@Override
	public int getBirth()
	{
		if (!isDBOnline())
		{
			return birth;
		}
		else
		{
			checkForDBUpdate();
			return (Integer) properties.getProperty("birth");
		}		
	}

	@Override
	public int getDeath()
	{
		if (!isDBOnline())
		{
			return death;
		}
		else
		{
			checkForDBUpdate();
			return (Integer) properties.getProperty("death");
		}
	}
	
	@Override
	public void update(ICelestialBody celestialBodyUpdate)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			super.update(celestialBodyUpdate);
			
			if (!IVortex.class.isInstance(celestialBodyUpdate)) throw new RuntimeException("Illegal vortex update, not a vortex instance.");
			
			IVortex vortexUpdate = (IVortex) celestialBodyUpdate;
			
			prepareUpdate();
			properties.setProperty("birth", vortexUpdate.getBirth());
			properties.setProperty("death", vortexUpdate.getDeath());			
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}		
	}

}
