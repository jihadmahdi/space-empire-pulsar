package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.orm.Building;
import org.axan.sep.common.db.orm.base.IBasePulsarLaunchingPad;
import org.axan.sep.common.db.orm.base.BasePulsarLaunchingPad;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IPulsarLaunchingPad;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;

class PulsarLaunchingPad extends Building implements IPulsarLaunchingPad
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
	public PulsarLaunchingPad(String productiveCelestialBodyName, int builtDate, int nbSlots)
	{
		super(productiveCelestialBodyName, builtDate, nbSlots);
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param productiveCelestialBodyName
	 */
	public PulsarLaunchingPad(SEPCommonDB sepDB, String productiveCelestialBodyName)
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
		properties.setProperty("nbFired", 0);
	}
	
	@Override
	final protected void register(Node properties)
	{
		super.register(properties);
	}
	
	@Override
	public int getNbFired()
	{
		assertOnlineStatus(true);
		
		return (Integer) properties.getProperty("nbFired");
	}
	
	public static double getTotalBonus(int nbFreshSlots)
	{
		return Double.valueOf(nbFreshSlots)* (double) 0.25;
	}
	
	public static int getUpgradeCarbonCost(int nbSlots)
	{
		return (int) (1+nbSlots * 0.25) * 1000;
	}
	
	public static int getUpgradePopulationCost(int nbSlots)
	{
		return (int) (1+nbSlots * 0.25) * 1000;
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void update(IBuilding buildingUpdate)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			super.update(buildingUpdate);
			
			if (!IPulsarLaunchingPad.class.isInstance(buildingUpdate)) throw new RuntimeException("Illegal pulsar launching pad update, not a pulsar launching pad instance.");
			
			IPulsarLaunchingPad pulsarLaunchingPadUpdate = (IPulsarLaunchingPad) buildingUpdate;
			
			if (getNbSlots() < pulsarLaunchingPadUpdate.getNbFired()) throw new RuntimeException("Illegal pulsar launching pad update, nbSlots < nbFired.");
			
			prepareUpdate();
			properties.setProperty("nbFired", pulsarLaunchingPadUpdate.getNbFired());
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
		StringBuilder sb = new StringBuilder();
		
		if (!isDBOnline())
		{
			sb.append("db off");
			return sb.toString();
		}
		
		checkForDBUpdate();
		
		int nbFired = getNbFired();
		int nbFreshSlots = getNbSlots()-nbFired;		
		sb.append(String.format("%d pulsar launching pads ready t fire with a power bonus of %.2f, %d already used.\n", nbFreshSlots, getTotalBonus(nbFreshSlots), nbFired));
		
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
