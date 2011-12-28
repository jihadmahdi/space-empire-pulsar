package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.IBaseBuilding;
import org.axan.sep.common.db.orm.base.BaseBuilding;
import org.axan.sep.common.db.IBuilding;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;

abstract class Building extends AGraphObject implements IBuilding
{
	/*
	 * PK: first pk field (composed pk).
	 */
	protected final String productiveCelestialBodyName;
	protected final eBuildingType type;
	
	/*
	 * Off-DB fields.
	 */
	protected final int builtDate;
	protected final int nbSlots;
	
	/*
	 * DB connection
	 * DB connected and permanent value only. i.e. Indexes and Factories only (no value, no node, no relation).
	 */
	protected Index<Node> buildingIndex;
	protected Index<Node> productiveCelestialBodyIndex;
	
	/**
	 * Off-DB constructor.
	 * @param productiveCelestialBodyName
	 * @param builtDate
	 * @param slots
	 */
	public Building(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		super(String.format("%s;$class", productiveCelestialBodyName));
		this.productiveCelestialBodyName = productiveCelestialBodyName;
		this.type = eBuildingType.valueOf(this.getClass().getSimpleName());
		this.builtDate = builtDate;
		this.nbSlots = nbSlots;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param productiveCelestialBodyName
	 */
	public Building(SEPCommonDB sepDB, String productiveCelestialBodyName)
	{
		super(sepDB, String.format("%s;$class", productiveCelestialBodyName));
		this.productiveCelestialBodyName = productiveCelestialBodyName;
		this.type = eBuildingType.valueOf(this.getClass().getSimpleName());
		
		// Null values
		this.builtDate = 0;
		this.nbSlots = 0;
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
			
			buildingIndex = db.index().forNodes("BuildingIndex");
			IndexHits<Node> hits = buildingIndex.get("productiveCelestialBodyName;class", String.format("%s;%s", productiveCelestialBodyName, type));
			node = hits.hasNext() ? hits.getSingle() : null;
			if (node != null && !node.getProperty("type").equals(type.toString()))
			{
				throw new RuntimeException("Node type error: tried to connect '"+type+"' to '"+node.getProperty("type")+"'");
			}
			productiveCelestialBodyIndex = db.index().forNodes("ProductiveCelestialBodyIndex");			
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
			if (buildingIndex.get("productiveCelestialBodyName;class", String.format("%s;%s", productiveCelestialBodyName, type)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, building[productiveCelestialBodyNale='"+productiveCelestialBodyName+"';type='"+type+"'] already exist.");
			}
			buildingIndex.add(node, "productiveCelestialBodyName;class", String.format("%s;%s", productiveCelestialBodyName, type));
			
			IndexHits<Node> hits = productiveCelestialBodyIndex.get("name", productiveCelestialBodyName);
			if (!hits.hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find ProductiveCelestialBody[name='"+productiveCelestialBodyName+"']. ProductiveCelestialBody must be created before Building.");
			}
			
			Node nProductiveCelestialBody = hits.getSingle();
			nProductiveCelestialBody.createRelationshipTo(node, eRelationTypes.Buildings);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public String getProductiveCelestialBodyName()
	{
		return productiveCelestialBodyName;
	}
	
	@Override
	public eBuildingType getType()
	{
		return type;
	}
	
	@Override
	public int getBuiltDate()
	{
		if (!isDBOnline())
		{
			return builtDate;
		}
		else
		{
			checkForDBUpdate();
			return (Integer) node.getProperty("builtDate");
		}
	}

	@Override
	public int getNbSlots()
	{
		if (!isDBOnline())
		{
			return nbSlots;
		}
		else
		{
			checkForDBUpdate();
			return (Integer) node.getProperty("nbSlots");
		}
	}

}
