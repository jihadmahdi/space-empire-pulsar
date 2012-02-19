package org.axan.sep.common.db.orm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.Events.AGameEvent;
import org.axan.sep.common.db.Events.UpdateArea;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.IProbeMarker;
import org.axan.sep.common.db.IUnitMarker;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

public class Probe extends Unit implements IProbe
{
	protected static final String querySeriePK(String ownerName, String serie)
	{
		return String.format("%s@%s*", ownerName, serie);
	}
	
	/*
	 * PK: inherited
	 */
	
	/*
	 * Off-DB fields.
	 */
	
	/*
	 * DB connection
	 */
	private transient Index<Node> probeIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 * @param productiveCelestialBodyName
	 */
	Probe(String ownerName, String serieName, int serialNumber, String productiveCelestialBodyName)
	{
		super(ownerName, String.format("%s-%d", serieName, serialNumber), productiveCelestialBodyName);
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param ownerName
	 * @param name
	 */
	Probe(SEPCommonDB sepDB, String ownerName, String name)
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
			probeIndex = db.index().forNodes("ProbeIndex");			
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
			
			if (probeIndex.get(PK, getPK(ownerName, name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field '"+PK+"' must be unique, probe["+getPK(ownerName, name)+"] already exist.");
			}			
			
			properties = sepDB.getDB().createNode();
			Probe.initializeProperties(properties, ownerName, name, initialDepartureName, departure);
			probeIndex.add(properties, PK, getPK(ownerName, name));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get("name", ownerName).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}
			//nOwner.createRelationshipTo(properties, eUnitType.Probe);
			
			super.create(sepDB);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void destroy()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		boolean isDeployed = isDeployed();
		Location center = getRealLocation().asLocation();
		float sight = getSight();
		
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{			
			probeIndex.remove(properties);
			//properties.getSingleRelationship(eUnitType.Probe, Direction.INCOMING).delete();			
			
			super.destroy();
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
		
		if (isDeployed)
		{
			for(Location l : SEPUtils.scanSphere(center, (int) sight))
			{
				sepDB.getArea(l);
				sepDB.fireAreaChangedEvent(l);
			}
		}
	}
	
	@Override
	public IProbeMarker getMarker(double step)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return new ProbeMarker(getTurn(), ownerName, getSerieName(), getSerialNumber(), isStopped(), getRealLocation(step), getSpeed(), isDeployed());
	}
	
	@Override
	public String getSerieName()
	{
		return name.substring(0, name.lastIndexOf('-'));
	}
	
	@Override
	public int getSerialNumber()
	{	
		return Integer.valueOf(name.substring(name.lastIndexOf('-')+1));
	}
	
	@Override
	public boolean isDeployed()
	{		
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return (isStopped() && (!sepDB.getCelestialBody(getInitialDepartureName()).getLocation().equals(getDeparture())));
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void onArrival(IGameEventExecutor executor)
	{
		super.onArrival(executor);
		
		if (AGameEvent.isGlobalView(executor))
		{
			Location center = getRealLocation().asLocation();
			float sight = getSight();
			
			for(Location l : SEPUtils.scanSphere(center, (int) sight))
			{
				IArea area = sepDB.getArea(l);
				UpdateArea updateArea = new UpdateArea(area);
				executor.onGameEvent(updateArea, new HashSet<String>(Arrays.asList(ownerName)));
			}
		}			
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());		
		if (!isDBOnline()) return sb.toString();
		sb.append("Status : "+(isDeployed()?"deployed":"not deployed"));
		return sb.toString();
	}
	
	public static void initializeProperties(Node properties, String ownerName, String name, String initialDepartureName, Location departure)
	{
		properties.setProperty("ownerName", ownerName);
		properties.setProperty("name", name);
		properties.setProperty("type", eUnitType.Probe.toString());
		properties.setProperty("initialDepartureName", initialDepartureName);
		properties.setProperty("departure", departure.toString());
	}
}
