package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.CelestialBody;
import org.axan.sep.common.db.orm.base.IBaseVortex;
import org.axan.sep.common.db.orm.base.BaseVortex;
import org.axan.sep.common.db.IVortex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

class Vortex extends CelestialBody implements IVortex
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
	protected Index<Node> vortexIndex;
	
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
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			super.checkForDBUpdate();			
			vortexIndex = db.index().forNodes("VortexIndex");			
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
			
			if (vortexIndex.get("name", name.toString()).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, vortex[name='"+name+"'] already exist.");
			}
			node = sepDB.getDB().createNode();
			Vortex.initializeNode(node, name, type, birth, death);
			vortexIndex.add(node, "name", name);
			
			super.create(sepDB);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
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
			return (Integer) node.getProperty("birth");
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
			return (Integer) node.getProperty("death");
		}
	}

	public static void initializeNode(Node node, String name, eCelestialBodyType type, int birth, int death)
	{
		node.setProperty("name", name);
		node.setProperty("type", type.toString());
		node.setProperty("birth", birth);
		node.setProperty("death", death);
	}

}