package org.axan.sep.common.db.orm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.eplib.utils.Basic;
import org.axan.sep.common.Rules;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.Rules.StarshipTemplate;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IAntiProbeMissile;
import org.axan.sep.common.db.IAntiProbeMissileMarker;
import org.axan.sep.common.db.IProbeMarker;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.IUnitMarker;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.Events.AntiProbeMissileExplosion;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

public class AntiProbeMissile extends Unit implements IAntiProbeMissile
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
	private transient Index<Node> antiProbeMissileIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public AntiProbeMissile(String ownerName, String serieName, int serialNumber, String productiveCelestialBodyName)
	{
		super(ownerName, String.format("%s-%d", serieName, serialNumber), productiveCelestialBodyName);
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param ownerName
	 * @param name
	 */
	public AntiProbeMissile(SEPCommonDB sepDB, String ownerName, String name)
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
			antiProbeMissileIndex = db.index().forNodes("AntiProbeMissileIndex");			
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
			
			if (antiProbeMissileIndex.get(PK, getPK(ownerName, name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field '"+PK+"' must be unique, antiProbeMissile["+getPK(ownerName, name)+"] already exist.");
			}			
			
			properties = sepDB.getDB().createNode();
			AntiProbeMissile.initializeProperties(properties, ownerName, name, initialDepartureName, departure);
			antiProbeMissileIndex.add(properties, PK, getPK(ownerName, name));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get(Player.PK, Player.getPK(ownerName)).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}
			//nOwner.createRelationshipTo(properties, eUnitType.AntiProbeMissile);
			
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
		
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			antiProbeMissileIndex.remove(properties);
			//properties.getSingleRelationship(eUnitType.AntiProbeMissile, Direction.INCOMING).delete();
			
			super.destroy();
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
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
	public boolean isFired()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return getTarget() != null;
	}
	
	@Override
	public void setTarget(IProbeMarker target)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = db.beginTx();
		
		try
		{
			if (IProbe.class.isInstance(target))
			{
				Probe probe = (Probe) target;
				ProbeMarker marker = (ProbeMarker) sepDB.getUnitMarker(target.getTurn(), target.getStep(), target.getOwnerName(), target.getName(), eUnitType.Probe);
				
				if (marker == null)
				{
					marker = (ProbeMarker) probe.getMarker(0);
					marker.create(sepDB);
				}				
			}
			
			properties.setProperty("targetTurn", target.getTurn());
			properties.setProperty("targetStep", target.getStep());
			properties.setProperty("targetOwnerName", target.getOwnerName());
			properties.setProperty("targetName", target.getName());
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public IProbeMarker getTarget()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		if (!properties.hasProperty("targetTurn")) return null;
		int targetTurn = (Integer) properties.getProperty("targetTurn");
		if (!properties.hasProperty("targetStep")) return null;
		double targetStep = (Double) properties.getProperty("targetStep");
		if (!properties.hasProperty("targetOwnerName")) return null;
		String targetOwnerName = (String) properties.getProperty("targetOwnerName");
		if (!properties.hasProperty("targetName")) return null;
		String targetName = (String) properties.getProperty("targetName");
		
		return (IProbeMarker) sepDB.getUnitMarker(targetTurn, targetStep, targetOwnerName, targetName, eUnitType.Probe);		
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());		
		if (!isDBOnline()) return sb.toString();
		
		checkForDBUpdate();

		IProbeMarker target = getTarget();
		sb.append("Status : "+(isFired()?String.format("fired to '%s@%s' (%s)", target.getOwnerName(), target.getName(), getDestination()) : "not fired"));
		return sb.toString();
	}
	
	@Override
	public IAntiProbeMissileMarker getMarker(double step)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return new AntiProbeMissileMarker(getTurn(), step, ownerName, getSerieName(), getSerialNumber(), isStopped(), getRealLocation(step), getSpeed(), isFired());
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void onArrival(IGameEventExecutor executor)
	{		
		IProbeMarker target = getTarget(); // Marker version
		executor.onGameEvent(new AntiProbeMissileExplosion(getRealLocation().asLocation(), getOwnerName(), getName(), target.getOwnerName(), target.getName()), sepDB.getPlayersNames());		
	}
	
	public static void initializeProperties(Node properties, String ownerName, String name, String initialDepartureName, Location departure)
	{
		properties.setProperty("ownerName", ownerName);
		properties.setProperty("name", name);
		properties.setProperty("type", eUnitType.AntiProbeMissile.toString());
		properties.setProperty("initialDepartureName", initialDepartureName);
		properties.setProperty("departure", departure.toString());
	}
}
