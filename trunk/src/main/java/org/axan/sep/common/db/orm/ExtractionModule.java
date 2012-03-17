package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.orm.Building;
import org.axan.sep.common.db.orm.base.IBaseExtractionModule;
import org.axan.sep.common.db.orm.base.BaseExtractionModule;
import org.axan.sep.common.db.IExtractionModule;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;

class ExtractionModule extends Building implements IExtractionModule
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
	public ExtractionModule(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		super(productiveCelestialBodyName, builtDate, nbSlots);
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param productiveCelestialBodyName
	 */
	public ExtractionModule(SEPCommonDB sepDB, String productiveCelestialBodyName)
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
		super.register(properties);
	}
	
	public static int getCarbonProductionPerTurn(int nbSlots)
	{
		return (int) (5000 * (Double.valueOf(nbSlots) * (double) 0.25));
	}
	
	@Override
	public int getCarbonProductionPerTurn()
	{
		return getCarbonProductionPerTurn(getNbSlots());
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
		sb.append(String.format("%d extraction modules built, extract %dC per turn.\n", nbSlots, getCarbonProductionPerTurn(nbSlots)));
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
}
