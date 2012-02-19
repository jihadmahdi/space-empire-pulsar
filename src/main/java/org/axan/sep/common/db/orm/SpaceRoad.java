package org.axan.sep.common.db.orm;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.ISpaceRoad;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

public class SpaceRoad extends AGraphObject<Relationship> implements ISpaceRoad
{

	/*
	 * PK
	 */
	protected String sourceName;
	protected String destinationName;
	
	/*
	 * Off-DB fields.
	 */
	
	/*
	 * DB connection
	 */
	private transient Index<Relationship> spaceRoadIndex;
	
	/**
	 * Off-DB constructor.
	 * @param sourceName
	 * @param destinationName
	 * @param speed
	 */
	public SpaceRoad(String sourceName, String destinationName)
	{
		super(String.format("%s->%s", sourceName, destinationName));
		this.sourceName = sourceName;
		this.destinationName = destinationName;
	}
	
	public SpaceRoad(SEPCommonDB sepDB, String sourceName, String destinationName)
	{
		super(sepDB, String.format("%s->%s", sourceName, destinationName));
		this.sourceName = sourceName;
		this.destinationName = destinationName;
	}
	
	/**
	 * If object is DB connected, check for DB update.
	 */
	@Override
	protected void checkForDBUpdate()
	{
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			db = sepDB.getDB();
			
			spaceRoadIndex = db.index().forRelationships("SpaceRoadIndex");
			IndexHits<Relationship> hits = spaceRoadIndex.get("source->destination", String.format("%s->%s", sourceName, destinationName));
			properties = hits.hasNext() ? hits.getSingle() : null;
		}
	}

	/**
	 * Current object must be Off-DB to call create method.
	 * Create method connect the object to the given DB and create the object node.
	 * After this call, object is DB connected.
	 * @param sepDB
	 */
	@Override
	protected void create(SEPCommonDB sepDB)
	{
		assertOnlineStatus(false, "Illegal state: can only call create(SEPCommonDB) method on Off-DB objects.");		
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			this.sepDB = sepDB;
			checkForDBUpdate();
			
			if (spaceRoadIndex.get("source->destination", String.format("%s->%s", sourceName, destinationName)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'source->destination' must be unique, spaceRoad[source->destination='"+sourceName+"->"+destinationName+"'] already exist.");
			}
			
			Index<Node> buildingIndex = sepDB.getDB().index().forNodes("BuildingIndex");			
			Node nSource = buildingIndex.get("productiveCelestialBodyName;class", String.format("%s;%s", sourceName, eBuildingType.SpaceCounter)).getSingle();
			if (nSource == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: No space counter found on celestial body named '"+sourceName+"' found.");
			}
			
			Node nDestination = buildingIndex.get("productiveCelestialBodyName;class", String.format("%s;%s", destinationName, eBuildingType.SpaceCounter)).getSingle();
			if (nDestination == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: No space counter found on celestial body named '"+destinationName+"' found.");
			}

			properties = nSource.createRelationshipTo(nDestination, eRelationTypes.SpaceRoad);
			SpaceRoad.initializeProperties(properties, sourceName, destinationName);
			spaceRoadIndex.add(properties, "source->destination", String.format("%s->%s", sourceName, destinationName));			
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void destroy()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{			
			spaceRoadIndex.remove(properties);
			properties.delete();
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public String getSourceName()
	{
		return sourceName;
	}
	
	@Override
	public String getDestinationName()
	{
		return destinationName;
	}
	
	@Override
	public String toString()
	{
		return sourceName+" -> "+destinationName;
	}

	public static void initializeProperties(Relationship properties, String sourceName, String destinationName)
	{
		properties.setProperty("sourceName", sourceName);
		properties.setProperty("destinationName", destinationName);
	}	
	
}
