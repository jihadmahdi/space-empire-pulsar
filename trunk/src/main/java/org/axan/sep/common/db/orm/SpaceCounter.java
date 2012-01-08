package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.orm.Building;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.IBaseSpaceCounter;
import org.axan.sep.common.db.orm.base.BaseSpaceCounter;
import org.axan.sep.common.db.ISpaceCounter;
import org.axan.sep.common.db.ISpaceRoad;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.axan.sep.common.db.IGameConfig;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

class SpaceCounter extends Building implements ISpaceCounter
{
	// PK inherited.
	
	/*
	 * Off-DB: off db fields (none).
	 */
	
	/*
	 * DB connection
	 */
	protected Index<Node> spaceCounterIndex;
	
	/**
	 * Off-DB constructor.
	 * @param productiveCelestialBodyName
	 * @param builtDate
	 * @param nbSlots
	 */
	public SpaceCounter(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		super(productiveCelestialBodyName, builtDate, nbSlots);
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param productiveCelestialBodyName
	 */
	public SpaceCounter(SEPCommonDB sepDB, String productiveCelestialBodyName)
	{
		super(sepDB, productiveCelestialBodyName);
	}

	@Override
	final protected void checkForDBUpdate()
	{				
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			super.checkForDBUpdate();			
			spaceCounterIndex = db.index().forNodes("SpaceCounterIndex");			
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
			
			if (spaceCounterIndex.get("productiveCelestialBodyName;class", String.format("%s;%s", productiveCelestialBodyName, type)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, spaceCounter[productiveCelestialBodyNale='"+productiveCelestialBodyName+"';type='"+type+"'] already exist.");
			}
			properties = sepDB.getDB().createNode();
			SpaceCounter.initializeProperties(properties, builtDate, nbSlots);
			spaceCounterIndex.add(properties, "productiveCelestialBodyName;class", String.format("%s;%s", productiveCelestialBodyName, type));
			
			super.create(sepDB);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	public Set<ISpaceRoad> getBuiltSpaceRoads()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Set<ISpaceRoad> result = new LinkedHashSet<ISpaceRoad>();
		
		String sourceName = (String) properties.getSingleRelationship(eRelationTypes.Buildings, Direction.INCOMING).getStartNode().getProperty("name");
		
		for(Relationship r : properties.getRelationships(Direction.OUTGOING, eRelationTypes.SpaceRoad))
		{
			String destinationName = (String) r.getEndNode().getSingleRelationship(eRelationTypes.Buildings, Direction.INCOMING).getStartNode().getProperty("name");					
			ISpaceRoad spaceRoad = new SpaceRoad(sepDB, sourceName, destinationName);
			result.add(spaceRoad);
		}
		
		return result;
	}
	
	public Set<ISpaceRoad> getLinkedSpaceRoads()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Set<ISpaceRoad> result = new LinkedHashSet<ISpaceRoad>();
		
		String destinationName = (String) properties.getSingleRelationship(eRelationTypes.Buildings, Direction.INCOMING).getStartNode().getProperty("name");
		
		for(Relationship r : properties.getRelationships(Direction.INCOMING, eRelationTypes.SpaceRoad))
		{
			String sourceName = (String) r.getStartNode().getSingleRelationship(eRelationTypes.Buildings, Direction.INCOMING).getStartNode().getProperty("name");					
			ISpaceRoad spaceRoad = new SpaceRoad(sepDB, sourceName, destinationName);
			result.add(spaceRoad);
		}
		
		return result;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		if (!isDBOnline())
		{
			sb.append("db off");
			return sb.toString();
		}
		
		checkForDBUpdate();
		
		int nbSlots = getNbSlots();		
		
		sb.append(nbSlots+" space counter(s) build.\n");
		boolean first=true;
		for(ISpaceRoad r : getBuiltSpaceRoads())
		{
			if (first) sb.append("Space Roads built :\n");
			sb.append("  "+r.toString()+"\n");
			first=false;
		}
		
		first=true;
		for(ISpaceRoad r : getLinkedSpaceRoads())
		{
			if (first) sb.append("Space Roads linked :\n");			
			sb.append("  "+r.toString()+"\n");
			first=false;
		}
		
		/* TODO: implement carbon orders
		 * 
		 *
		if (ordersToReceive.size() > 0)
		{
			sb.append("To receive :\n");
			for(CarbonCarrier o : ordersToReceive)
			{
				sb.append("  "+o+"\n");
			}
		}
		if (currentSentOrder.size() > 0)
		{
			sb.append("sent :\n");
			for(CarbonCarrier o : currentSentOrder)
			{
				sb.append("  "+o+"\n");
			}
		}
		if (nextOrders.size() > 0)
		{
			sb.append("next send :\n");
			for(CarbonOrder o : nextOrders)
			{
				sb.append("  "+o+"\n");
			}
		}
		*/
		
		return sb.toString();
	}

	public static void initializeProperties(Node properties, int builtDate, int nbSlots)
	{
		properties.setProperty("type", eBuildingType.SpaceCounter.toString());
		properties.setProperty("builtDate", builtDate);
		properties.setProperty("nbSlots", nbSlots);
	}

}
