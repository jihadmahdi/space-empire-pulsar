package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.ProductiveCelestialBody;
import org.axan.sep.common.db.orm.base.IBasePlanet;
import org.axan.sep.common.db.orm.base.BasePlanet;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.axan.sep.common.db.IGameConfig;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;

class Planet extends ProductiveCelestialBody implements IPlanet
{
	// PK inherited.
	
	/*
	 * Off-DB: off db fields.
	 */
	private int populationPerTurn;
	private int maxPopulation;
	private int currentPopulation;
	
	/*
	 * DB connection
	 */
	
	/**
	 * Off-DB constructor.
	 * @param name
	 * @param initialCarbonStock
	 * @param maxSlots
	 * @param carbonStock
	 * @param currentCarbon
	 * @param populationPerTurn
	 * @param maxPopulation
	 * @param currentPopulation
	 */
	public Planet(String name, Location location, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon, int populationPerTurn, int maxPopulation, int currentPopulation)
	{
		super(name, location, initialCarbonStock, maxSlots, carbonStock, currentCarbon);
		this.populationPerTurn = populationPerTurn;
		this.maxPopulation = maxPopulation;
		this.currentPopulation = currentPopulation;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param name
	 */
	public Planet(SEPCommonDB sepDB, String name)
	{
		super(sepDB, name);
		
		// Null values
		this.populationPerTurn = 0;
		this.maxPopulation = 0;
		this.currentPopulation = 0;
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
		properties.setProperty("populationPerTurn", populationPerTurn);
		properties.setProperty("maxPopulation", maxPopulation);
		properties.setProperty("currentPopulation", currentPopulation);
	}
	
	/**
	 * Register properties (add Node to indexes and create relationships).
	 * @param properties
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	final protected void register(Node properties)
	{
		super.register(properties);
	}

	@Override
	public int getPopulationPerTurn()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			return (Integer) properties.getProperty("populationPerTurn");
		}
		else
		{
			return populationPerTurn;
		}
	}

	@Override
	public int getMaxPopulation()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			return (Integer) properties.getProperty("maxPopulation");
		}
		else
		{
			return maxPopulation;
		}
	}

	@Override
	public int getCurrentPopulation()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			return (Integer) properties.getProperty("currentPopulation");
		}
		else
		{
			return currentPopulation;
		}
	}
	
	@Override
	public void payPopulation(int populationCost)
	{
		assertOnlineStatus(true);
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			checkForDBUpdate();
			if (getCurrentPopulation() < populationCost) throw new RuntimeException("Cannot pay population cost, not enough population");
			prepareUpdate();
			properties.setProperty("currentPopulation", getCurrentPopulation() - populationCost);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public void generatePopulation(int generatedPopulation)
	{
		assertOnlineStatus(true);
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			checkForDBUpdate();
			if ((getMaxPopulation() - getCurrentPopulation()) < generatedPopulation) throw new RuntimeException("Cannot generate off-limit population");
			prepareUpdate();
			properties.setProperty("currentPopulation", getCurrentPopulation() + generatedPopulation);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public void update(ICelestialBody celestialBodyUpdate)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			super.update(celestialBodyUpdate);
			
			if (!IPlanet.class.isInstance(celestialBodyUpdate)) throw new RuntimeException("Illegal planet update, not a planet instance.");
			
			IPlanet planetUpdate = (IPlanet) celestialBodyUpdate;
			
			prepareUpdate();
			properties.setProperty("populationPerTurn", planetUpdate.getPopulationPerTurn());
			properties.setProperty("maxPopulation", planetUpdate.getMaxPopulation());
			properties.setProperty("currentPopulation", planetUpdate.getCurrentPopulation());
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{		
		this.populationPerTurn = getPopulationPerTurn();
		this.maxPopulation = getMaxPopulation();
		this.currentPopulation = getCurrentPopulation();
		out.defaultWriteObject();
	}
	
	@Override
	public String toString()
	{
		return super.toString().replace("  Carbon : ", "  Population : "+getCurrentPopulation()+" (+"+getPopulationPerTurn()+" per turn) / "+getMaxPopulation()+"\n  Carbon : ");		
	}
}
