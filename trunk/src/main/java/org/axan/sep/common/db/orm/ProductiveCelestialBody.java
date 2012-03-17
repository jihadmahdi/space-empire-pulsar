package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.AGraphObject;
import org.axan.eplib.orm.nosql.AVersionedGraphNode;
import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.CelestialBody;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.BasePlanet;
import org.axan.sep.common.db.orm.base.IBaseProductiveCelestialBody;
import org.axan.sep.common.db.orm.base.BaseProductiveCelestialBody;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.axan.sep.common.db.IGameConfig;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.OverridingMethodsMustInvokeSuper;

abstract class ProductiveCelestialBody extends CelestialBody implements IProductiveCelestialBody
{
	/*
	 * PK (inherited): first pk field.
	 */
	
	/*
	 * Off-DB fields
	 */
	protected int initialCarbonStock;
	protected int maxSlots;
	protected int carbonStock;
	protected int currentCarbon;
	protected String ownerName;
	
	/*
	 * Serialization fields.
	 */
	protected Set<IBuilding> serializedBuildings = new HashSet<IBuilding>();
	
	/*
	 * DB connection: DB connection and useful objects (e.g. indexes and nodes).
	 */
	
	/**
	 * Off-DB constructor.
	 * @param name
	 * @param initialCarbonStock
	 * @param maxSlots
	 * @param carbonStock
	 * @param currentCarbon
	 */
	public ProductiveCelestialBody(String name, Location location, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon)
	{
		super(name, location);
		this.initialCarbonStock = initialCarbonStock;
		this.maxSlots = maxSlots;
		this.carbonStock = carbonStock;
		this.currentCarbon = currentCarbon;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param name
	 */
	public ProductiveCelestialBody(SEPCommonDB sepDB, String name)
	{
		super(sepDB, name);
		
		// Null values
		this.initialCarbonStock = 0;
		this.maxSlots = 0;
		this.carbonStock = 0;
		this.currentCarbon = 0;
	}
	
	@Override
	protected void initializeProperties()
	{
		super.initializeProperties();
		properties.setProperty("initialCarbonStock", initialCarbonStock);
		properties.setProperty("maxSlots", maxSlots);
		properties.setProperty("carbonStock", carbonStock);
		properties.setProperty("currentCarbon", currentCarbon);
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
			updateOwnership();
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	/**
	 * DB connection is not checked, but object must be connected, or connecting (inside {@link #create(SEPCommonDB)} call).
	 */
	private void updateOwnership()
	{
		Relationship oldOwnership = getLastSingleRelationship(eRelationTypes.PlayerCelestialBodies, Direction.INCOMING);
		
		if (oldOwnership == null && ownerName != null || ownerName == null && oldOwnership != null || ownerName != null && oldOwnership != null && !ownerName.equals((String) oldOwnership.getStartNode().getProperty("name")))
		{
			prepareUpdate();
			oldOwnership = getLastSingleRelationship(eRelationTypes.PlayerCelestialBodies, Direction.INCOMING);
		}
		
		if (ownerName == null)
		{
			// Delete ownership relation
			if (oldOwnership != null) oldOwnership.delete();
			/*
			ownership = properties.getSingleRelationship(type, Direction.INCOMING);
			if (ownership != null) ownership.delete();
			*/			
		}
		else if (oldOwnership == null || !ownerName.equals((String) oldOwnership.getStartNode().getProperty("name")))
		{
			// Delete previous and Create ownership relation				
			if (oldOwnership != null)
			{
				oldOwnership.delete();
				oldOwnership = null;
			}
			
			Node nOwner = AGraphObject.get(graphDB.getDB().index().forNodes("PlayerIndex"), Player.getPK(ownerName));
			nOwner.createRelationshipTo(properties, eRelationTypes.PlayerCelestialBodies); // checked
			//nOwner.createRelationshipTo(properties, type);
		}
	}
	
	@Override
	public String getOwner()
	{
		if (isDBOnline())		
		{
			checkForDBUpdate();
			Relationship ownership = properties == null ? null : getLastSingleRelationship(eRelationTypes.PlayerCelestialBodies, Direction.INCOMING);
			Node nOwner = ownership == null ? null : ownership.getStartNode();
			ownerName = nOwner == null ? null : (String) nOwner.getProperty("name");
		}
		
		return ownerName;
	}
	
	@Override
	public void setOwner(String ownerName)
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			
			Transaction tx = graphDB.getDB().beginTx();
			try
			{
				this.ownerName = ownerName;
				updateOwnership();
				tx.success();
			}
			finally
			{
				tx.finish();
			}
		}
		else
		{
			this.ownerName = ownerName;
		}		
	}

	@Override
	public int getInitialCarbonStock()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			return (Integer) properties.getProperty("initialCarbonStock");
		}
		else
		{
			return initialCarbonStock;
		}
	}

	@Override
	public int getMaxSlots()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			return (Integer) properties.getProperty("maxSlots");
		}
		else
		{
			return maxSlots;
		}
	}

	@Override
	public int getCarbonStock()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			return (Integer) properties.getProperty("carbonStock");
		}
		else
		{
			return carbonStock;
		}
	}

	@Override
	public int getCurrentCarbon()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			return (Integer) properties.getProperty("currentCarbon");
		}
		else
		{
			return currentCarbon;
		}
	}
	
	@Override
	public void payCarbon(int carbonCost)
	{
		assertOnlineStatus(true);
		
		if (carbonCost == 0) return;
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{			
			checkForDBUpdate();
			if (getCurrentCarbon() < carbonCost) throw new RuntimeException("Cannot pay carbon cost, not enough carbon");
			prepareUpdate();
			properties.setProperty("currentCarbon", getCurrentCarbon() - carbonCost);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public void extractCarbon(int extractedCarbon)
	{
		assertOnlineStatus(true);
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			checkForDBUpdate();
			if (getCarbonStock() < extractedCarbon) throw new RuntimeException("Cannot extract carbon, not enough stock");
			prepareUpdate();
			properties.setProperty("carbonStock", getCarbonStock() - extractedCarbon);
			properties.setProperty("currentCarbon", getCurrentCarbon() + extractedCarbon);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public int getBuiltSlotsCount()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		int result=0;
		
		for(Relationship r : getLastRelationships(eRelationTypes.Buildings, Direction.OUTGOING))
		{
			result += (Integer) r.getEndNode().getProperty("nbSlots");
		}
		
		return result;
	}
	
	@Override
	public IBuilding getBuilding(eBuildingType type)
	{
		assertOnlineStatus(true);		
		return graphDB.getBuilding(getName(), type);
	}
	
	@Override
	public Set<IBuilding> getBuildings()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			
			Set<IBuilding> result = new HashSet<IBuilding>();
			for(Relationship r : getLastRelationships(eRelationTypes.Buildings, Direction.OUTGOING))			
			{
				Node n = r.getEndNode();
				result.add(graphDB.getBuilding(name, eBuildingType.valueOf((String) n.getProperty("type"))));
			}
			return result;
		}
		else
		{
			return serializedBuildings;
		}
	}
	
	/**
	 * Get assigned fleet for current productive celestial body and given player name.
	 * If not exist and create flag is true, create and return it.
	 * If not exist and create flag is false, return null.
	 * @param playerName
	 * @return
	 */
	@Override
	public IFleet getAssignedFleet(String playerName)
	{
		return getAssignedFleet(playerName, false);
	}
	
	IFleet getAssignedFleet(final String playerName, boolean create)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Node nFleet = null;
		for(Relationship r : getLastRelationships(eRelationTypes.AssignedFleets, Direction.OUTGOING))
		{
			if (!r.hasProperty("playerName")) continue;
			if (!playerName.equals(r.getProperty("playerName"))) continue;
			nFleet = r.getEndNode();
			break;
		}
				
		if (nFleet == null)
		{
			if (!create) return null;
			
			assertLastVersion();
			
			Transaction tx = graphDB.getDB().beginTx();
			
			try
			{
				String fleetName = String.format("%s assigned fleet", getName());
				graphDB.createFleet(graphDB.makeFleet(playerName, fleetName, name, new HashMap<StarshipTemplate, Integer>()));
				
				nFleet = AVersionedGraphNode.queryVersion(graphDB.getDB().index().forNodes("FleetIndex"), Unit.getPK(playerName, fleetName), graphDB.getVersion());
				
				if (nFleet == null)
				{
					throw new RuntimeException("Implementation error, cannot get just created assigned fleet "+playerName+"@"+fleetName);
				}
				
				Relationship rel = properties.createRelationshipTo(nFleet, eRelationTypes.AssignedFleets); // checked
				rel.setProperty("playerName", playerName);
				
				tx.success();
			}
			finally
			{
				tx.finish();
			}						
		}
		
		String fleetName = (String) nFleet.getProperty("name");
		return new Fleet(graphDB, playerName, fleetName);
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
			
			if (!IProductiveCelestialBody.class.isInstance(celestialBodyUpdate)) throw new RuntimeException("Illegal productive celestial body update, not a productive celestial body instance.");
			
			IProductiveCelestialBody productiveCelestialBodyUpdate = (IProductiveCelestialBody) celestialBodyUpdate;
			
			if (getInitialCarbonStock() != productiveCelestialBodyUpdate.getInitialCarbonStock()) throw new RuntimeException("Illegal productive celestial body update, initial carbon stock value is inconsistent.");
			if (getMaxSlots() != productiveCelestialBodyUpdate.getMaxSlots()) throw new RuntimeException("Illegal productive celestial body update, max slots value is inconsistent.");
			
			prepareUpdate();
			
			properties.setProperty("carbonStock", productiveCelestialBodyUpdate.getCarbonStock());
			properties.setProperty("currentCarbon", productiveCelestialBodyUpdate.getCurrentCarbon());
			
			// Owner
			setOwner(productiveCelestialBodyUpdate.getOwner());
			
			// Buildings
			Set<IBuilding> buildingsUpdate = productiveCelestialBodyUpdate.getBuildings();
			
			for(IBuilding building : getBuildings())
			{
				boolean found = false;
				for(IBuilding buildingUpdate : buildingsUpdate)
				{
					if (building.getType() == buildingUpdate.getType())
					{
						found = true;
						building.update(buildingUpdate);
						buildingsUpdate.remove(buildingUpdate);
						break;
					}
				}
				
				if (!found)
				{					
					// Remove building
					((Building) building).delete();
				}
			}
			
			for(IBuilding buildingUpdate : buildingsUpdate)
			{
				// Add building
				graphDB.createBuilding(getName(), buildingUpdate.getBuiltDate(), buildingUpdate.getType()).update(buildingUpdate);
			}
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}		
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{		
		this.initialCarbonStock = getInitialCarbonStock();
		this.maxSlots = getMaxSlots();
		this.carbonStock = getCarbonStock();
		this.currentCarbon = getCurrentCarbon();
		this.ownerName = getOwner();
		this.serializedBuildings = getBuildings();
		out.defaultWriteObject();
	}
	
	@Override
	public String toString()
	{		
		StringBuilder sb = new StringBuilder();
		
		if (!isDBOnline())
		{
			sb.append("db off");
			return sb.toString();
		}
		
		checkForDBUpdate();
		sb.append(getOwner() == null ? "" : "["+getOwner()+"] ");
		sb.append(getName()+" ("+getType()+")\n");
		//if (attackEnemiesFleet) {sb.append("Enemies fleet will be attacked next turn\n");}
		sb.append("  Carbon : "+getCurrentCarbon()+" / "+getCarbonStock()+" ("+getInitialCarbonStock()+")\n");
		sb.append("  Slots : "+getBuiltSlotsCount()+" / "+getMaxSlots()+"\n");
		
		boolean first=true;
		for(IBuilding b : getBuildings())
		{
			if (first) sb.append("  Buildings :\n");
			sb.append("    "+b.getType()+" : "+b.getNbSlots()+"\n");
			first=false;
		}
		
		return sb.toString();
	}

}
