package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.AVersionedGraphNode;
import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Rules;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.IBaseBuilding;
import org.axan.sep.common.db.orm.base.BaseBuilding;
import org.axan.sep.common.db.IBuilding;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;

abstract class Building extends AVersionedGraphNode<SEPCommonDB> implements IBuilding
{
	public static final String getPK(String productiveCelestialBodyName, eBuildingType type)
	{
		return String.format("%s;%s", productiveCelestialBodyName, type);
	}
	
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
	
	@Override
	protected void initializeProperties()
	{
		super.initializeProperties();
		properties.setProperty("productiveCelestialBodyName", productiveCelestialBodyName);
		properties.setProperty("type", eBuildingType.DefenseModule.toString());
		properties.setProperty("builtDate", builtDate);
		properties.setProperty("nbSlots", nbSlots);
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
			
			Node nProductiveCelestialBody = AVersionedGraphNode.queryVersion(graphDB.getDB().index().forNodes("ProductiveCelestialBodyIndex"), CelestialBody.getPK(productiveCelestialBodyName), graphDB.getVersion());
			if (nProductiveCelestialBody == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find ProductiveCelestialBody[name='"+productiveCelestialBodyName+"']. ProductiveCelestialBody must be created before Building.");
			}			
			nProductiveCelestialBody.createRelationshipTo(properties, eRelationTypes.Buildings); // checked
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void demolish()
	{
		delete();
	}
	
	@Override
	protected void delete()
	{
		super.delete();
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
			return (Integer) properties.getProperty("builtDate");
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
			return (Integer) properties.getProperty("nbSlots");
		}
	}
	
	@Override
	public void upgrade()
	{
		assertOnlineStatus(true);
		
		if (!Rules.getBuildingCanBeUpgraded(getType())) throw new RuntimeException("Cannot upgrade '"+getType()+"'");
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			// TODO: Check for productive celestial body free slots condition ?
			prepareUpdate();
			properties.setProperty("nbSlots", getNbSlots()+1);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public void downgrade()
	{
		assertOnlineStatus(true);
		
		if (!Rules.getBuildingCanBeDowngraded(this)) throw new RuntimeException("Cannot downgrade '"+getType()+"'");
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			// TODO: Check for productive celestial body free slots condition ?
			prepareUpdate();
			properties.setProperty("nbSlots", getNbSlots()-1);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void update(IBuilding buildingUpdate)
	{
		assertLastVersion();
		checkForDBUpdate();
		
		if (getType() != buildingUpdate.getType()) throw new RuntimeException("Illegal building update, inconsistent type.");
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			prepareUpdate();
			properties.setProperty("builtDate", buildingUpdate.getBuiltDate());
			properties.setProperty("nbSlots", buildingUpdate.getNbSlots());
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
}
