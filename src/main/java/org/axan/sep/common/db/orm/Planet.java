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
			node = sepDB.getDB().createNode();
			Planet.initializeNode(node, name, type, initialCarbonStock, maxSlots, carbonStock, currentCarbon, populationPerTurn, maxPopulation, currentPopulation);
			planetIndex.add(node, "name", name);
			
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
			return (Integer) node.getProperty("populationPerTurn");
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
			return (Integer) node.getProperty("maxPopulation");
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
			return (Integer) node.getProperty("currentPopulation");
		}
		else
		{
			return currentPopulation;
		}
	}
	
	@Override
	public String toString()
	{
		return super.toString().replace("  Carbon : ", "  Population : "+getCurrentPopulation()+" (+"+getPopulationPerTurn()+" per turn) / "+getMaxPopulation()+"\n  Carbon : ");		
	}

	public static void initializeNode(Node node, String name, eCelestialBodyType type, int initialCarbonStock, int maxSlots, int carbonStock, int currentCarbon, int populationPerTurn, int maxPopulation, int currentPopulation)
	{
		node.setProperty("name", name);
		node.setProperty("type", type.toString());
		node.setProperty("initialCarbonStock", initialCarbonStock);
		node.setProperty("maxSlots", maxSlots);
		node.setProperty("carbonStock", carbonStock);
		node.setProperty("currentCarbon", currentCarbon);
		node.setProperty("populationPerTurn", populationPerTurn);
		node.setProperty("maxPopulation", maxPopulation);
		node.setProperty("currentPopulation", currentPopulation);
	}
}
