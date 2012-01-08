package org.axan.sep.common.db.orm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.eplib.utils.Basic;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.Rules;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

public class Fleet extends Unit implements IFleet
{
	/*
	 * PK: inherited
	 */
	
	/*
	 * Off-DB fields.
	 */
	private final Map<StarshipTemplate, Integer> starships = new HashMap<StarshipTemplate, Integer>();
	
	/*
	 * DB connection
	 */
	private transient Index<Node> fleetIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public Fleet(String ownerName, String name, String productiveCelestialBodyName, Map<StarshipTemplate, Integer> starships)
	{
		super(ownerName, name, productiveCelestialBodyName);
		this.starships.putAll(starships);
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param ownerName
	 * @param name
	 */
	public Fleet(SEPCommonDB sepDB, String ownerName, String name)
	{
		super(sepDB, ownerName, name);
	}
	
	@Override
	final protected void checkForDBUpdate()
	{				
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			super.checkForDBUpdate();
			fleetIndex = db.index().forNodes("FleetIndex");			
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
			
			if (fleetIndex.get("ownerName@name", String.format("%s@%s", ownerName, name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'ownerName@name' must be unique, fleet[ownerName='"+ownerName+"', name='"+name+"'] already exist.");
			}			
			
			properties = sepDB.getDB().createNode();
			Fleet.initializeProperties(properties, ownerName, name, initialDepartureName, departure, starships);
			fleetIndex.add(properties, "ownerName@name", String.format("%s@%s", ownerName, name));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get("name", ownerName).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}
			nOwner.createRelationshipTo(properties, eUnitType.Fleet);
			
			super.create(sepDB);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public synchronized void addStarships(Map<StarshipTemplate, Integer> newcomers)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = db.beginTx();
		
		try
		{						
			getStarships();
			
			for(StarshipTemplate template : newcomers.keySet())
			{
				int newComersQuantity = newcomers.get(template);
				if (newComersQuantity < 0) throw new RuntimeException("Newcomers quantity cannot be negative.");
				int quantity = (starships.containsKey(template) ? starships.get(template) : 0) + newComersQuantity;
				starships.put(template, quantity);
			}
			
			initializeStarships(properties, starships);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public synchronized void removeStarships(Map<StarshipTemplate, Integer> leavers)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = db.beginTx();
		
		try
		{
			getStarships();
			
			for(StarshipTemplate template : leavers.keySet())
			{
				int leaversQuantity = leavers.get(template);
				if (leaversQuantity < 0) throw new RuntimeException("Leavers quantity cannot be negative.");
				int quantity = (starships.containsKey(template) ? starships.get(template) : 0) - leaversQuantity;
				if (quantity < 0) throw new RuntimeException("Leavers quantity cannot be greater than current quantity ("+template.getName()+")");
				starships.put(template, quantity);
			}
			
			initializeStarships(properties, starships);			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public Map<StarshipTemplate, Integer> getStarships()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			
			for(StarshipTemplate template : Rules.getStarshipTemplates())
			{
				if (properties.hasProperty("starships"+template.getName()))
				{
					starships.put(template, (Integer) properties.getProperty("starships"+template.getName()));
				}
				else
				{
					starships.put(template, 0);
				}
			}
		}
		
		return Collections.unmodifiableMap(starships);
	}
	
	/*
	@Override
	public boolean isEmpty()
	{
		for(int quantity : getStarships().values())
		{
			if (quantity > 0) return false;
		}
		return true;
	}
	*/
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		if (!isDBOnline())
		{
			// TODO: implement offline version ?
			sb.append("db offline");
			return sb.toString();
		}
		
		checkForDBUpdate();
		
		sb.append(super.toString());
		sb.append("Fleet composition :\n");
		for(Map.Entry<StarshipTemplate, Integer> e : getStarships().entrySet())
		{
			if (e.getValue() != null && e.getValue() > 0)
			{
				sb.append("  "+Basic.padRight(e.getKey().getName(), 15, ' ')+"\t"+e.getValue()+"\n");
			}
		}
		
		return sb.toString();
	}
	
	public static void initializeProperties(Node properties, String ownerName, String name, String initialDepartureName, Location departure, Map<StarshipTemplate, Integer> starships)
	{
		properties.setProperty("ownerName", ownerName);
		properties.setProperty("name", name);
		properties.setProperty("type", eUnitType.Fleet.toString());
		properties.setProperty("initialDepartureName", initialDepartureName);
		properties.setProperty("departure", departure.toString());
		initializeStarships(properties, starships);
	}
	
	public static void initializeStarships(Node properties, Map<StarshipTemplate, Integer> starships)
	{
		int quantity = 0;
		for(StarshipTemplate template : Rules.getStarshipTemplates())		
		{
			quantity = (starships.containsKey(template)) ? starships.get(template) : 0;
			properties.setProperty("starships"+template.getName(), quantity);
		}
	}
}
