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
		super.checkForDBUpdate();		
	}
	
	@Override
	final protected void initializeProperties()
	{
		super.initializeProperties();
		Fleet.initializeStarships(this, starships);
		properties.setProperty("isAssignedFleet", isAssignedFleet);
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
	
	Node getProperties()
	{
		return properties;
	}
	
	@Override
	protected void prepareUpdate()
	{
		super.prepareUpdate();
	}
}
