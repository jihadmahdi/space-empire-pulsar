package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.AVersionedGraphNode;
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
		super.checkForDBUpdate();
	}
	
	@Override
	final protected void initializeProperties()
	{
		super.initializeProperties();		
	}
	
	@Override
	final protected void register(Node properties)
	{
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			super.register(properties);
			
			Node nProductiveCelestialBody = getLastSingleRelationship(eRelationTypes.Buildings, Direction.INCOMING).getStartNode();
			Relationship r = AVersionedGraphNode.getLastSingleRelationship(graphDB, nProductiveCelestialBody, graphDB.getVersion(), eRelationTypes.PlayerCelestialBodies, Direction.INCOMING);			
			Node nOwner = r == null ? null : r.getStartNode();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find productive celestial body owner.");
			}
			
			Government government = new Government((String) nOwner.getProperty("name"), (String) nProductiveCelestialBody.getProperty("name"));
			government.create(graphDB);			
			
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

}
