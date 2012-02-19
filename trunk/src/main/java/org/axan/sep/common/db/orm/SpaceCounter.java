package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.SEPCommonImplementationException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.Building;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.IBaseSpaceCounter;
import org.axan.sep.common.db.orm.base.BaseSpaceCounter;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IPulsarLaunchingPad;
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
import java.util.Stack;
import java.util.Vector;

import javax.annotation.OverridingMethodsMustInvokeSuper;

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
			SpaceCounter.initializeProperties(properties, productiveCelestialBodyName, builtDate, nbSlots);
			spaceCounterIndex.add(properties, "productiveCelestialBodyName;class", String.format("%s;%s", productiveCelestialBodyName, type));
			
			super.create(sepDB);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void delete()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			for(ISpaceRoad bsr : getBuiltSpaceRoads())
			{
				bsr.destroy();
			}
			
			for(ISpaceRoad lsr : getLinkedSpaceRoads())
			{
				lsr.destroy();
			}
			
			spaceCounterIndex.remove(properties);
			super.delete();
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public void buildSpaceRoad(String destinationName)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		if (getNbSlots() - getBuiltSpaceRoads().size() < 1) throw new RuntimeException("Illegal buildSpaceRoad call, no more space roads to build.");
		
		new SpaceRoad(getProductiveCelestialBodyName(), destinationName).create(sepDB);
	}
	
	@Override
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
	
	@Override
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
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void update(IBuilding buildingUpdate)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			super.update(buildingUpdate);
			
			if (!ISpaceCounter.class.isInstance(buildingUpdate)) throw new RuntimeException("Illegal space counter update, not a pulsar launching pad instance.");
			
			ISpaceCounter spaceCounterUpdate = (ISpaceCounter) buildingUpdate;
			
			// Built space roads
			Set<ISpaceRoad> builtSpaceRoadsUpdate = spaceCounterUpdate.getBuiltSpaceRoads();
			Stack<ISpaceRoad> builtSpaceRoads = new Stack<ISpaceRoad>();
			builtSpaceRoads.addAll(getBuiltSpaceRoads());
			
			while(!builtSpaceRoads.isEmpty())
			{				
				ISpaceRoad spaceRoad = builtSpaceRoads.pop();

				boolean found = false;
				for(ISpaceRoad spaceRoadUpdate : builtSpaceRoadsUpdate)
				{
					if (spaceRoad.getDestinationName().equals(spaceRoadUpdate.getDestinationName()))
					{
						found = true;
						builtSpaceRoadsUpdate.remove(spaceRoadUpdate);
						break;
					}
				}
				
				if (!found)
				{
					// Remove
					spaceRoad.destroy();
				}
			}
			
			for(ISpaceRoad spaceRoadUpdate : builtSpaceRoadsUpdate)
			{
				// Add
				IProductiveCelestialBody destination = (IProductiveCelestialBody) sepDB.getCelestialBody(spaceRoadUpdate.getDestinationName());
				ISpaceCounter destinationSpaceCounter = (ISpaceCounter) destination.getBuilding(eBuildingType.SpaceCounter);
				if (destinationSpaceCounter == null)
				{
					// Create hypothesis space counter, which will be updated on destination observation.
					destinationSpaceCounter = (ISpaceCounter) sepDB.createBuilding(destination.getName(), sepDB.getConfig().getTurn(), eBuildingType.SpaceCounter);
				}
				
				buildSpaceRoad(destination.getName());
			}
			
			// Linked space roads
			Set<ISpaceRoad> linkedSpaceRoadsUpdate = spaceCounterUpdate.getLinkedSpaceRoads();
			Stack<ISpaceRoad> linkedSpaceRoads = new Stack<ISpaceRoad>();
			linkedSpaceRoads.addAll(getLinkedSpaceRoads());
			
			while(!linkedSpaceRoads.isEmpty())
			{				
				ISpaceRoad spaceRoad = builtSpaceRoads.pop();

				boolean found = false;
				for(ISpaceRoad spaceRoadUpdate : linkedSpaceRoadsUpdate)
				{
					if (spaceRoad.getSourceName().equals(spaceRoadUpdate.getSourceName()))
					{
						found = true;
						linkedSpaceRoadsUpdate.remove(spaceRoadUpdate);
						break;
					}
				}
				
				if (!found)
				{
					// Remove
					spaceRoad.destroy();
				}
			}
			
			for(ISpaceRoad spaceRoadUpdate : linkedSpaceRoadsUpdate)
			{
				// Add
				IProductiveCelestialBody source = (IProductiveCelestialBody) sepDB.getCelestialBody(spaceRoadUpdate.getSourceName());
				ISpaceCounter sourceSpaceCounter = (ISpaceCounter) source.getBuilding(eBuildingType.SpaceCounter);
				if (sourceSpaceCounter == null)
				{
					// Create hypothesis space counter, which will be updated on destination observation.
					sourceSpaceCounter = (ISpaceCounter) sepDB.createBuilding(source.getName(), sepDB.getConfig().getTurn(), eBuildingType.SpaceCounter);
				}
								
				if (sourceSpaceCounter.getNbSlots() - sourceSpaceCounter.getBuiltSpaceRoads().size() < 1) // To match this condition, only 0 < 1 is expected.
				{
					// Hypothetically upgrade the source space counter so we can add space road. The true space counter will be updated whence source is observed.
					sourceSpaceCounter.upgrade();
				}
				
				sourceSpaceCounter.buildSpaceRoad(getProductiveCelestialBodyName());
			}
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
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

	public static void initializeProperties(Node properties, String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		properties.setProperty("productiveCelestialBodyName", productiveCelestialBodyName);
		properties.setProperty("type", eBuildingType.SpaceCounter.toString());
		properties.setProperty("builtDate", builtDate);
		properties.setProperty("nbSlots", nbSlots);
	}

}
