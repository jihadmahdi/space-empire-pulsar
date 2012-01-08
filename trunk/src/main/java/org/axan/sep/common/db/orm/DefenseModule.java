package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.orm.Building;
import org.axan.sep.common.db.orm.base.IBaseDefenseModule;
import org.axan.sep.common.db.orm.base.BaseDefenseModule;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.IDefenseModule;
import org.javabuilders.annotations.Built;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

class DefenseModule extends Building implements IDefenseModule
{
	// PK inherited.
	
	/*
	 * Off-DB: off db fields (none).
	 */
	
	/*
	 * DB connection
	 */
	protected Index<Node> defenseModuleIndex;
	
	/**
	 * Off-DB constructor.
	 * @param productiveCelestialBodyName
	 * @param builtDate
	 * @param nbSlots
	 */
	public DefenseModule(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		super(productiveCelestialBodyName, builtDate, nbSlots);
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param productiveCelestialBodyName
	 */
	public DefenseModule(SEPCommonDB sepDB, String productiveCelestialBodyName)
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
			defenseModuleIndex = db.index().forNodes("DefenseModuleIndex");			
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
						
			if (defenseModuleIndex.get("productiveCelestialBodyName;class", String.format("%s;%s", productiveCelestialBodyName, type)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, defenseModule[productiveCelestialBodyNale='"+productiveCelestialBodyName+"';type='"+type+"'] already exist.");
			}
			properties = sepDB.getDB().createNode();
			DefenseModule.initializeProperties(properties, builtDate, nbSlots);
			defenseModuleIndex.add(properties, "productiveCelestialBodyName;class", String.format("%s;%s", productiveCelestialBodyName, type));
			
			super.create(sepDB);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}	

	public static double getTotalBonus(int nbSlots)
	{
		return (int) (0.25 * nbSlots * 100);
	}
	
	public static int getUpgradeCarbonCost(int nbSlots)
	{
		return (int) ((Double.valueOf(1+nbSlots) * 0.25) * 1000);
	}
	
	public static int getUpgradePopulationCost(int nbSlots)
	{
		return 0;
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
		
		int nbSlots = getNbSlots();
		sb.append(String.format("%d defense modules built, give a defense bonus of %.2f.\n", nbSlots, getTotalBonus(nbSlots)));
		if (getUpgradeCarbonCost(nbSlots) < 0 && getUpgradePopulationCost(nbSlots) < 0)
		{
			sb.append("Cannot build more");
		}
		else
		{
			sb.append(String.format("Upgrade cost %dC, %dP.", getUpgradeCarbonCost(nbSlots), getUpgradePopulationCost(nbSlots)));
		}
		
		return sb.toString();		
	}
	
	public static void initializeProperties(Node properties, int builtDate, int nbSlots)
	{
		properties.setProperty("type", eBuildingType.DefenseModule.toString());
		properties.setProperty("builtDate", builtDate);
		properties.setProperty("nbSlots", nbSlots);
	}

}
