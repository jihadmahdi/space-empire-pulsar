package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.orm.Building;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.IBaseGovernmentModule;
import org.axan.sep.common.db.orm.base.BaseGovernmentModule;
import org.axan.sep.common.db.IGovernmentModule;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;

class GovernmentModule extends Building implements IGovernmentModule
{
	// PK inherited.
	
	/*
	 * Off-DB: off db fields (none).
	 */
	
	/*
	 * DB connection
	 */
	protected Index<Node> governmentModuleIndex;
	
	/**
	 * Off-DB constructor.
	 * @param productiveCelestialBodyName
	 * @param builtDate
	 * @param nbSlots
	 */
	public GovernmentModule(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		super(productiveCelestialBodyName, builtDate, nbSlots);
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param productiveCelestialBodyName
	 */
	public GovernmentModule(SEPCommonDB sepDB, String productiveCelestialBodyName)
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
			governmentModuleIndex = db.index().forNodes("GovernmentModuleIndex");			
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
			
			if (governmentModuleIndex.get(PK, getPK(productiveCelestialBodyName, type)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, governmentModule[productiveCelestialBodyNale='"+productiveCelestialBodyName+"';type='"+type+"'] already exist.");
			}
			properties = sepDB.getDB().createNode();
			GovernmentModule.initializeProperties(properties, productiveCelestialBodyName, builtDate, nbSlots);
			governmentModuleIndex.add(properties, PK, getPK(productiveCelestialBodyName, type));
			
			Node nProductiveCelestialBody = productiveCelestialBodyIndex.get(CelestialBody.PK, CelestialBody.getPK(productiveCelestialBodyName)).getSingle();
			if (nProductiveCelestialBody == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find productive celestial body.");
			}
			
			Relationship r = nProductiveCelestialBody.getSingleRelationship(eRelationTypes.PlayerCelestialBodies, Direction.INCOMING);			
			Node nOwner = r == null ? null : r.getStartNode();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find productive celestial body owner.");
			}
			
			r = nOwner.getSingleRelationship(eRelationTypes.PlayerGovernment, Direction.OUTGOING);
			if (r != null)
			{
				throw new DBGraphException("Cannot redefine PlayerGovernment because relationship already exist.");
			}
			
			nOwner.createRelationshipTo(properties, eRelationTypes.PlayerGovernment);
			
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
			governmentModuleIndex.remove(properties);
			super.delete();
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public String toString()
	{
		return "Government module gives a +50% bonus to population per turn and +50% bonus to carbon resource extraction on the current planet.";			
	}	
	
	public static void initializeProperties(Node properties, String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		properties.setProperty("productiveCelestialBodyName", productiveCelestialBodyName);
		properties.setProperty("type", eBuildingType.GovernmentModule.toString());
		properties.setProperty("builtDate", builtDate);
		properties.setProperty("nbSlots", nbSlots);
	}

}
