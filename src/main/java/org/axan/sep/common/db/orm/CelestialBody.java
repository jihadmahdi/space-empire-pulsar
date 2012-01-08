package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.IBaseCelestialBody;
import org.axan.sep.common.db.orm.base.BaseCelestialBody;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.ICelestialBody;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;

abstract class CelestialBody extends AGraphObject<Node> implements ICelestialBody
{
	/*
	 * PK: first pk field.
	 */
	protected final String name;
		
	/*
	 * Off-DB fields (none)
	 */
	protected final Location location;
	protected final eCelestialBodyType type;
	
	/*
	 * DB connection: DB connection and useful objects (e.g. indexes and nodes).
	 */
	protected Index<Node> celestialBodyIndex;	
	
	/**
	 * Off-DB constructor.
	 * Off-DB constructor is full params off-db.
	 * @param name
	 */
	public CelestialBody(String name, Location location)
	{
		super(name);
		this.name = name;
		this.location = location;
		this.type = eCelestialBodyType.valueOf(getClass().getSimpleName());
	}
	
	/**
	 * On-DB constructor.
	 * On-DB constructor require only DB object and first pk field value.
	 * @param sepDB
	 * @param name
	 */
	public CelestialBody(SEPCommonDB sepDB, String name)
	{
		super(sepDB, name);
		this.name = name;
		this.type = eCelestialBodyType.valueOf(getClass().getSimpleName());
		
		// Null values
		this.location = null;
	}
	
	/**
	 * If object is DB connected, check for DB update.
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	protected void checkForDBUpdate()
	{
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			db = sepDB.getDB();
			
			celestialBodyIndex = db.index().forNodes("CelestialBodyIndex");
			IndexHits<Node> hits = celestialBodyIndex.get("name", name);
			properties = hits.hasNext() ? hits.getSingle() : null;			
			if (properties != null && !properties.getProperty("type").equals(type.toString()))
			{
				throw new RuntimeException("Node type error: tried to connect '"+type+"' to '"+properties.getProperty("type")+"'");
			}
		}
	}
	
	/**
	 * Current object must be Off-DB to call create method.
	 * Create method connect the object to the given DB and create the object node.
	 * After this call, object is DB connected.
	 * @param sepDB
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	protected void create(SEPCommonDB sepDB)
	{
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			if (celestialBodyIndex.get("name", name).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, celestialBody[name='"+name+"'] already exist.");
			}
			celestialBodyIndex.add(properties, "name", name);
			
			// Force area creation if not exists.
			sepDB.getArea(location);
			
			IndexHits<Node> hits = sepDB.getDB().index().forNodes("AreaIndex").get("location", location.toString());
			if (!hits.hasNext())
			{
				tx.failure();
				throw new DBGraphException("Implementation error: Cannot find Area[location='"+location.toString()+"']. Area must be created before CelestialBody.");				
			}
			
			Node nArea = hits.getSingle();
			
			nArea.createRelationshipTo(properties, eRelationTypes.CelestialBody);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public eCelestialBodyType getType()
	{
		return type;
	}
	
	@Override
	public Location getLocation()
	{
		if (!isDBOnline())
		{
			return location;
		}
		else
		{
			checkForDBUpdate();
			Node nArea = properties.getSingleRelationship(eRelationTypes.CelestialBody, Direction.INCOMING).getStartNode();
			return Location.valueOf((String) nArea.getProperty("location"));
		}
	}
}
