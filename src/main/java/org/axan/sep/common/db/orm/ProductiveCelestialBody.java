package org.axan.sep.common.db.orm;

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
	protected transient Index<Node> productiveCelestialBodyIndex;
	protected transient Index<Node> buildingIndex;
	
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
	@OverridingMethodsMustInvokeSuper
	protected void checkForDBUpdate()
	{				
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			super.checkForDBUpdate();			
			productiveCelestialBodyIndex = db.index().forNodes("ProductiveCelestialBodyIndex");
			buildingIndex = db.index().forNodes("BuildingIndex");			
		}
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	protected void create(SEPCommonDB sepDB)
	{
		Transaction tx = sepDB.getDB().beginTx();		
		
		try
		{
			if (productiveCelestialBodyIndex.get(PK, getPK(name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, productiveCelestialBody[name='"+name+"'] already exist.");
			}
			productiveCelestialBodyIndex.add(properties, PK, getPK(name));
			
			updateOwnership();
			
			super.create(sepDB);
			
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
		Transaction tx = db.beginTx();
		
		try
		{
			Relationship ownership = properties.getSingleRelationship(eRelationTypes.PlayerCelestialBodies, Direction.INCOMING);
			
			if (ownerName == null)
			{
				// Delete ownership relation
				if (ownership != null) ownership.delete();
				/*
				ownership = properties.getSingleRelationship(type, Direction.INCOMING);
				if (ownership != null) ownership.delete();
				*/			
			}
			else if (ownership == null || !ownerName.equals((String) ownership.getStartNode().getProperty("name")))
			{
				// Delete previous and Create ownership relation				
				if (ownership != null)
				{
					ownership.delete();
					ownership = null;
				}
				
				Node nOwner = db.index().forNodes("PlayerIndex").get(Player.PK, Player.getPK(ownerName)).getSingle();
				nOwner.createRelationshipTo(properties, eRelationTypes.PlayerCelestialBodies);			
				//nOwner.createRelationshipTo(properties, type);
			}
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public String getOwner()
	{
		if (isDBOnline())		
		{
			checkForDBUpdate();
			Relationship ownership = properties == null ? null : properties.getSingleRelationship(eRelationTypes.PlayerCelestialBodies, Direction.INCOMING);
			Node nOwner = ownership == null ? null : ownership.getStartNode();
			ownerName = nOwner == null ? null : (String) nOwner.getProperty("name");
		}
		
		return ownerName;
	}
	
	@Override
	public void setOwner(String ownerName)
	{
		this.ownerName = ownerName;
		if (isDBOnline())
		{
			checkForDBUpdate();
			updateOwnership();			
			checkForDBUpdate();
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
		
		Transaction tx = db.beginTx();
		
		try
		{
			checkForDBUpdate();
			if (getCurrentCarbon() < carbonCost) throw new RuntimeException("Cannot pay carbon cost, not enough carbon");
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
		
		Transaction tx = db.beginTx();
		
		try
		{
			checkForDBUpdate();
			if (getCarbonStock() < extractedCarbon) throw new RuntimeException("Cannot extract carbon, not enough stock");
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
		for(Node n : buildingIndex.query(Building.PK, Building.queryAnyBuildingTypePK(name)))
		{
			result += (Integer) n.getProperty("nbSlots");
		}
		
		return result;
	}
	
	@Override
	public IBuilding getBuilding(eBuildingType type)
	{
		assertOnlineStatus(true);		
		return sepDB.getBuilding(getName(), type);
	}
	
	@Override
	public Set<IBuilding> getBuildings()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			
			Set<IBuilding> result = new HashSet<IBuilding>();
			for(Node n : buildingIndex.query(Building.PK, Building.queryAnyBuildingTypePK(name)))
			{
				result.add(sepDB.getBuilding(name, eBuildingType.valueOf((String) n.getProperty("type"))));
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
		
		TraversalDescription td = Traversal.description().breadthFirst().relationships(eRelationTypes.AssignedFleets, Direction.OUTGOING).evaluator(new Evaluator()
		{
			
			@Override
			public Evaluation evaluate(Path path)
			{
				Relationship rel = path.lastRelationship();
				if (rel == null) return Evaluation.EXCLUDE_AND_CONTINUE;
				if (!rel.hasProperty("playerName")) return Evaluation.EXCLUDE_AND_PRUNE;
				if (!playerName.equals(rel.getProperty("playerName"))) return Evaluation.EXCLUDE_AND_PRUNE;
				return Evaluation.INCLUDE_AND_PRUNE;
			}
		});			
		
		Traverser traverser = td.traverse(properties);
		Node nFleet = traverser.nodes().iterator().hasNext() ? traverser.nodes().iterator().next() : null;
		
		if (nFleet == null)
		{
			if (!create) return null;
			
			Transaction tx = db.beginTx();
			
			try
			{
				String fleetName = String.format("%s assigned fleet", getName());
				sepDB.createFleet(sepDB.makeFleet(playerName, fleetName, name, new HashMap<StarshipTemplate, Integer>()));
				
				nFleet = db.index().forNodes("FleetIndex").get(Unit.PK, Unit.getPK(playerName, fleetName)).getSingle();
				
				if (nFleet == null)
				{
					throw new RuntimeException("Implementation error, cannot get just created assigned fleet "+playerName+"@"+fleetName);
				}
				
				Relationship rel = properties.createRelationshipTo(nFleet, eRelationTypes.AssignedFleets);
				rel.setProperty("playerName", playerName);
				
				tx.success();
			}
			finally
			{
				tx.finish();
			}
			
			// Double check
			nFleet = td.traverse(properties).nodes().iterator().next();
		}
		
		String fleetName = (String) nFleet.getProperty("name");
		return new Fleet(sepDB, playerName, fleetName);
	}
	
	@Override
	public void update(ICelestialBody celestialBodyUpdate)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			super.update(celestialBodyUpdate);
			
			if (!IProductiveCelestialBody.class.isInstance(celestialBodyUpdate)) throw new RuntimeException("Illegal productive celestial body update, not a productive celestial body instance.");
			
			IProductiveCelestialBody productiveCelestialBodyUpdate = (IProductiveCelestialBody) celestialBodyUpdate;
			
			if (getInitialCarbonStock() != productiveCelestialBodyUpdate.getInitialCarbonStock()) throw new RuntimeException("Illegal productive celestial body update, initial carbon stock value is inconsistent.");
			if (getMaxSlots() != productiveCelestialBodyUpdate.getMaxSlots()) throw new RuntimeException("Illegal productive celestial body update, max slots value is inconsistent.");
			
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
				sepDB.createBuilding(getName(), buildingUpdate.getBuiltDate(), buildingUpdate.getType()).update(buildingUpdate);
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
