package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.db.orm.Building;
import org.axan.sep.common.db.orm.base.IBaseStarshipPlant;
import org.axan.sep.common.db.orm.base.BaseStarshipPlant;
import org.axan.sep.common.db.IStarshipPlant;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

class StarshipPlant extends Building implements IStarshipPlant
{
	// PK inherited.
	
	/*
	 * Off-DB: off db fields (none).
	 */
	
	/*
	 * DB connection
	 */
	protected Index<Node> starshipPlantIndex;
	
	/**
	 * Off-DB constructor.
	 * @param productiveCelestialBodyName
	 * @param builtDate
	 * @param nbSlots
	 */
	public StarshipPlant(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		super(productiveCelestialBodyName, builtDate, nbSlots);
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param productiveCelestialBodyName
	 */
	public StarshipPlant(SEPCommonDB sepDB, String productiveCelestialBodyName)
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
			starshipPlantIndex = db.index().forNodes("StarshipPlantIndex");			
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
			
			if (starshipPlantIndex.get("productiveCelestialBodyName;class", String.format("%s;%s", productiveCelestialBodyName, type)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, starshipPlant[productiveCelestialBodyNale='"+productiveCelestialBodyName+"';type='"+type+"'] already exist.");
			}
			node = sepDB.getDB().createNode();
			StarshipPlant.initializeNode(node, builtDate, nbSlots);
			starshipPlantIndex.add(node, "productiveCelestialBodyName;class", String.format("%s;%s", productiveCelestialBodyName, type));
			
			super.create(sepDB);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}

	public static void initializeNode(Node node, int builtDate, int nbSlots)
	{
		node.setProperty("builtDate", builtDate);
		node.setProperty("nbSlots", nbSlots);
	}

}
