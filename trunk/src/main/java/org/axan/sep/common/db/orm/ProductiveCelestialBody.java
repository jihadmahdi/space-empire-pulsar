package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.CelestialBody;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.BasePlanet;
import org.axan.sep.common.db.orm.base.IBaseProductiveCelestialBody;
import org.axan.sep.common.db.orm.base.BaseProductiveCelestialBody;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IPlayerConfig;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.axan.sep.common.db.IGameConfig;

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
	protected final int initialCarbonStock;
	protected final int maxSlots;
	protected final int carbonStock;
	protected final int currentCarbon;
	protected String ownerName;
	
	/*
	 * DB connection: DB connection and useful objects (e.g. indexes and nodes).
	 */
	protected Index<Node> productiveCelestialBodyIndex;
	protected Index<Node> buildingIndex;
	
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
			if (productiveCelestialBodyIndex.get("name", name.toString()).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, productiveCelestialBody[name='"+name+"'] already exist.");
			}
			productiveCelestialBodyIndex.add(node, "name", name);
			
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
			Relationship ownership = node.getSingleRelationship(eRelationTypes.PlayerCelestialBodies, Direction.INCOMING);
			eCelestialBodyType type = eCelestialBodyType.valueOf(getClass().getSimpleName());
			
			if (ownerName == null)
			{
				// Delete ownership relation
				if (ownership != null) ownership.delete();
				ownership = node.getSingleRelationship(type, Direction.INCOMING);
				if (ownership != null) ownership.delete();			
			}
			else
			{
				// Delete any previous and Create ownership relation
				Node nOwner = db.index().forNodes("PlayerIndex").get("name", ownerName).getSingle();			
				if (ownership != null)
				{
					ownership.delete();
					ownership = null;
				}
				
				nOwner.createRelationshipTo(node, eRelationTypes.PlayerCelestialBodies);			
				nOwner.createRelationshipTo(node, type);
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
			Relationship ownership = node == null ? null : node.getSingleRelationship(eRelationTypes.PlayerCelestialBodies, Direction.INCOMING);
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
			return (Integer) node.getProperty("initialCarbonStock");
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
			return (Integer) node.getProperty("maxSlots");
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
			return (Integer) node.getProperty("carbonStock");
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
			return (Integer) node.getProperty("currentCarbon");
		}
		else
		{
			return currentCarbon;
		}
	}
	
	@Override
	public int getBuiltSlotsCount()
	{
		assertOnlineStatus(true);
		int result=0;
		for(Node n : buildingIndex.get("productiveCelestialBodyName;class", String.format("%s;*", name)))
		{
			result += (Integer) n.getProperty("nbSlots");
		}
		
		return result;
	}
	
	@Override
	public Set<IBuilding> getBuildings()
	{
		assertOnlineStatus(true);
		Set<IBuilding> result = new HashSet<IBuilding>();
		for(Node n : buildingIndex.get("productiveCelestialBodyName;class", String.format("%s;*", name)))
		{
			result.add(sepDB.getBuilding(name, eBuildingType.valueOf((String) n.getProperty("type"))));
		}
		return result;
	}
	
	@Override
	public String toString()
	{		
		StringBuffer sb = new StringBuffer();
		
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
