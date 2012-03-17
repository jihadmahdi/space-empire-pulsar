package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.AVersionedGraphNode;
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

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;

abstract class CelestialBody extends AVersionedGraphNode<SEPCommonDB> implements ICelestialBody
{
	public static final String getPK(String name)
	{
		return name;
	}
	
	/*
	 * PK: first pk field.
	 */
	protected final String name;
		
	/*
	 * Off-DB fields (none)
	 */
	protected Location location;
	protected final eCelestialBodyType type;
	
	/*
	 * DB connection: DB connection and useful objects (e.g. indexes and nodes).
	 */
	
	/**
	 * Off-DB constructor.
	 * Off-DB constructor is full params off-db.
	 * @param name
	 */
	public CelestialBody(String name, Location location)
	{
		super(getPK(name));
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
		super(sepDB, getPK(name));
		this.name = name;
		this.type = eCelestialBodyType.valueOf(getClass().getSimpleName());
		
		// Null values
		this.location = null;
	}
	
	@Override
	protected void initializeProperties()
	{
		super.initializeProperties();
		properties.setProperty("name", name);
		properties.setProperty("type", type.toString());		
	}
	
	/**
	 * Register properties (add Node to indexes and create relationships).
	 * @param properties
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	protected void register(Node properties)
	{
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			super.register(properties);
			
			// Force area creation if not exists.
			graphDB.getArea(location);
			
			Node nArea = queryVersion(graphDB.getDB().index().forNodes("AreaIndex"), Area.getPK(location), graphDB.getVersion());
			if (nArea == null)
			{
				tx.failure();
				throw new DBGraphException("Implementation error: Cannot find Area[location='"+location.toString()+"']. Area must be created before CelestialBody.");				
			}
			nArea.createRelationshipTo(properties, eRelationTypes.CelestialBody); // checked
			
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
			Node nArea = getLastSingleRelationship(eRelationTypes.CelestialBody, Direction.INCOMING).getStartNode();
			return Location.valueOf((String) nArea.getProperty("location"));
		}
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void update(ICelestialBody celestialBodyUpdate)
	{
		assertLastVersion();
		checkForDBUpdate();
		
		if (getType() != celestialBodyUpdate.getType()) throw new RuntimeException("Illegal celestial body update, inconsistent type.");
		if (!getName().equals(celestialBodyUpdate.getName())) throw new RuntimeException("Illegal celestial body update, inconsistent name.");
		if (!getLocation().equals(celestialBodyUpdate.getLocation())) throw new RuntimeException("Illegal celestial body update, inconsistent location.");
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{		
		location = getLocation();
		out.defaultWriteObject();
	}

}
