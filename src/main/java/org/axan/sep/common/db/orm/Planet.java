package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.orm.ProductiveCelestialBody;
import org.axan.sep.common.db.orm.base.IBasePlanet;
import org.axan.sep.common.db.orm.base.BasePlanet;
import org.axan.sep.common.db.IBuilding;
import org.axan.sep.common.db.IPlanet;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

class Planet extends ProductiveCelestialBody implements IPlanet
{
	// PK inherited.
	
	/*
	 * Off-DB: off db fields.
	 */
	private final int populationPerTurn;
	private final int maxPopulation;
	private final int currentPopulation;
	
	/*
	 * DB connection
	 */
	protected Index<Node> planetIndex;

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
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			super.checkForDBUpdate();			
			planetIndex = db.index().forNodes("PlanetIndex");			
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
			
			if (planetIndex.get("name", name.toString()).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'name' must be unique, planet[name='"+name+"'] already exist.");
			}
			properties = sepDB.getDB().createNode();
			Planet.initializeProperties(properties, name, initialCarbonStock, maxSlots, carbonStock, currentCarbon, populationPerTurn, maxPopulation, currentPopulation);
			planetIndex.add(properties, "name", name);
			
			super.create(sepDB);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
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
		
		Transaction tx = db.beginTx();
		
		try
		{
			checkForDBUpdate();
			if (getCurrentPopulation() < populationCost) throw new RuntimeException("Cannot pay population cost, not enough population");
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
		
		Transaction tx = db.beginTx();
		
		try
		{
			checkForDBUpdate();
			if ((getMaxPopulation() - getCurrentPopulation()) < generatedPopulation) throw new RuntimeException("Cannot generate off-limit population");
			properties.setProperty("currentPopulation", getCurrentPopulation() + generatedPopulation);
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
		return super.toString().replace("  Carbon : ", "  Population : "+getCurrentPopulation()+" (+"+getPopulationPerTurn()+" per turn) / "+getMaxPopulation()+"\n  Carbon : ");		
	}

	public static void initializeProperties(Node properties, String name, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon, int populationPerTurn, int maxPopulation, int currentPopulation)
	{
		properties.setProperty("name", name);
		properties.setProperty("type", eCelestialBodyType.Planet.toString());
		properties.setProperty("initialCarbonStock", initialCarbonStock);
		properties.setProperty("maxSlots", maxSlots);
		properties.setProperty("carbonStock", carbonStock);
		properties.setProperty("currentCarbon", currentCarbon);
		properties.setProperty("populationPerTurn", populationPerTurn);
		properties.setProperty("maxPopulation", maxPopulation);
		properties.setProperty("currentPopulation", currentPopulation);
	}
}
