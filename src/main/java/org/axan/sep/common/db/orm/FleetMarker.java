package org.axan.sep.common.db.orm;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.eplib.utils.Basic;
import org.axan.sep.common.Rules;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IFleetMarker;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

public class FleetMarker extends UnitMarker implements IFleetMarker
{
	/*
	 * PK: inherited
	 */
	
	/*
	 * Off-DB fields.
	 */
	private final Map<StarshipTemplate, Integer> starships = new HashMap<StarshipTemplate, Integer>();
	private boolean isAssignedFleet;
	
	/*
	 * DB connection
	 */
	private transient Index<Node> fleetMarkerIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public FleetMarker(int turn, double step, String ownerName, String name, boolean isStopped, RealLocation realLocation, float speed, Map<StarshipTemplate, Integer> starships, boolean isAssignedFleet)
	{
		super(turn, step, ownerName, name, isStopped, realLocation, speed);
		this.starships.putAll(starships);
		this.isAssignedFleet = isAssignedFleet;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param ownerName
	 * @param name
	 */
	public FleetMarker(SEPCommonDB sepDB, int turn, double step, String ownerName, String name)
	{
		super(sepDB, turn, step, ownerName, name);
		
		// Null values
		this.isAssignedFleet = false;
	}
	
	@Override
	final protected void checkForDBUpdate()
	{				
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			super.checkForDBUpdate();
			fleetMarkerIndex = db.index().forNodes("FleetMarkerIndex");			
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
			
			if (fleetMarkerIndex.get(PK, getPK(turn, step, ownerName, name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field '"+PK+"' must be unique, fleetMarker["+getPK(turn, step, ownerName, name)+"] already exist.");
			}			
			
			properties = sepDB.getDB().createNode();
			FleetMarker.initializeProperties(properties, turn, step, ownerName, name, isStopped, realLocation, speed, starships, isAssignedFleet);
			fleetMarkerIndex.add(properties, PK, getPK(turn, step, ownerName, name));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get(Player.PK, Player.getPK(ownerName)).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}			
			nOwner.createRelationshipTo(properties, DynamicRelationshipType.withName(eUnitType.Fleet + "Marker"));			
			
			super.create(sepDB);
			
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
	
	@Override
	public int getStarshipsCount()
	{		
		int qt = 0;
		for(int tqt : getStarships().values())
		{
			qt += tqt;
		}
		
		return qt;
	}
	
	@Override
	public boolean isAssignedFleet()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();			
			return (Boolean) properties.getProperty("isAssignedFleet");
		}
		
		return isAssignedFleet;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());		
		if (!isDBOnline()) return sb.toString(); // TODO: implement Fleet offline version ?		
		
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
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (!FleetMarker.class.isInstance(obj)) return false;
		FleetMarker fm = (FleetMarker) obj;
		if (!getStarships().equals(fm.getStarships())) return false;
		if (isAssignedFleet() != fm.isAssignedFleet()) return false;
		return true;
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public int hashCode()
	{
		return super.hashCode() + getStarships().hashCode() + (isAssignedFleet()?1:0);
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		getStarships();
		isAssignedFleet = isAssignedFleet();
		out.defaultWriteObject();
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}
	
	public static void initializeProperties(Node properties, int turn, double step, String ownerName, String name, boolean isStopped, RealLocation realLocation, float speed, Map<StarshipTemplate, Integer> starships, boolean isAssignedFleet)
	{
		properties.setProperty("turn", turn);
		properties.setProperty("step", step);
		properties.setProperty("ownerName", ownerName);
		properties.setProperty("name", name);
		properties.setProperty("type", eUnitType.Fleet.toString());
		properties.setProperty("isStopped", isStopped);		
		properties.setProperty("realLocation", realLocation.toString());
		properties.setProperty("speed", speed);
		Fleet.initializeStarships(properties, starships);
		properties.setProperty("isAssignedFleet", isAssignedFleet);
	}
}
