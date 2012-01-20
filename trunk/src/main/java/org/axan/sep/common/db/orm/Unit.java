package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.IBaseUnit;
import org.axan.sep.common.db.orm.base.BaseUnit;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.IUnitMarker;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;

abstract class Unit extends UnitMarker implements IUnit
{	
	protected static final String PK = "ownerName@name";
	protected static final String getPK(String ownerName, String name)
	{
		return String.format("%s@%s", ownerName, name);
	}
	
	/*
	 * PK: inherited
	 */
	
	/*
	 * Off-DB fields.
	 */	
	/** 
	 * Celestial body to spawn the unit to.
	 * It determines the initial value of departure.
	 * Celestial body must already exist.
	 */
	protected final String initialDepartureName;
	protected Location departure;	
	
	/*
	 * DB connection
	 */
	protected Index<Node> unitIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public Unit(String ownerName, String name, String productiveCelestialBodyName)
	{
		super(-1, ownerName, name, true, null, 0F);
		this.initialDepartureName = productiveCelestialBodyName;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param ownerName
	 * @param name
	 */
	public Unit(SEPCommonDB sepDB, String ownerName, String name)
	{
		super(sepDB, -1, ownerName, name);
		
		// Null values
		this.initialDepartureName = null;
	}
	
	/**
	 * If object is DB connected, check for DB update.
	 */
	@Override
	protected void checkForDBUpdate()
	{
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			db = sepDB.getDB();
			
			unitIndex = db.index().forNodes("UnitIndex");
			IndexHits<Node> hits = unitIndex.get(PK, getPK(ownerName, name));
			
			if (departure == null && initialDepartureName != null)
			{
				ICelestialBody cb = sepDB.getCelestialBody(initialDepartureName);
				if (cb == null || !IProductiveCelestialBody.class.isInstance(cb)) throw new RuntimeException("Unit initial location must be a valid productive celestial body, "+initialDepartureName+" "+(cb == null ? "does not exist" : "is not productive")+".");
				departure = cb.getLocation();
			}
			
			properties = hits.hasNext() ? hits.getSingle() : null;
			if (properties != null && !properties.getProperty("type").equals(type.toString()))
			{
				throw new RuntimeException("Node type error: tried to connect '"+type+"' to '"+properties.getProperty("type")+"'");
			}			
		}
	}

	/**
	 * Current object must be Off-DB to call create method.
	 * Create method connect the object to the given DB and create the object node.
	 * After this call, object is DB connected.
	 * @param sepDB
	 */
	@Override
	protected void create(SEPCommonDB sepDB)
	{
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			if (unitIndex.get(PK, getPK(ownerName, name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field '"+PK+"' must be unique, unit["+getPK(ownerName, name)+"] already exist.");
			}			
						
			unitIndex.add(properties, PK, getPK(ownerName, name));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get("name", ownerName).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}
			nOwner.createRelationshipTo(properties, eRelationTypes.PlayerUnit);
			
			// Ensure area creation
			updateDeparture();
			
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
			unitIndex.remove(properties);
			properties.getSingleRelationship(eRelationTypes.PlayerUnit, Direction.INCOMING).delete();
			properties.getSingleRelationship(eRelationTypes.UnitDeparture, Direction.OUTGOING).delete();
			
			Relationship destination = properties.getSingleRelationship(eRelationTypes.UnitDestination, Direction.OUTGOING);
			if (destination != null) destination.delete();
			
			for(Relationship encounterLog : properties.getRelationships(eRelationTypes.UnitEncounterLog, Direction.OUTGOING))
			{
				Node nEncounterLog = encounterLog.getEndNode();
				Relationship publication = nEncounterLog.getSingleRelationship(eRelationTypes.PlayerEncounterLog, Direction.INCOMING);
				
				if (publication == null)
				{
					encounterLog.delete();
					nEncounterLog.delete();
				}
			}
			
			properties.delete();
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public int getTurn()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return sepDB.getConfig().getTurn();
	}
	
	@Override
	public boolean isStopped()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return !properties.hasRelationship(eRelationTypes.UnitDestination, Direction.OUTGOING);
	}
	
	@Override
	public RealLocation getRealLocation()
	{
		Location destination = getDestination();
		if (destination == null) return getDeparture().asRealLocation();
		
		double progress = getTravelingProgress();
		if (progress >= 1) throw new RuntimeException("Progress should not be greater or equal to 1");
		return SEPUtils.getMobileLocation(getDeparture().asRealLocation(), destination.asRealLocation(), progress, true);				
	}
	
	/**
	 * Return unit real location during the current move at given step.
	 * @param step
	 * @return
	 */
	protected RealLocation getRealLocation(double step)
	{
		if (step < 0 || step > 1) throw new RuntimeException("Step is out of bounds [0; 1]");
		
		Location departure = getDeparture();
		Location destination = getDestination();
		if (destination == null) return getDeparture().asRealLocation();
		
		double progress = getTravelingProgress();
		if (progress >= 1) throw new RuntimeException("Progress should not be greater or equal to 1");
		return SEPUtils.getMobileLocation(getDeparture().asRealLocation(), destination.asRealLocation(), getSpeed(), progress, step, true);
	}
	
	@Override
	public float getSpeed()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return sepDB.getConfig().getUnitTypeSpeed(getType());
	}
	
	@Override
	public double getTravelingProgress()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return properties.hasProperty("travelingProgress") ? (Double) properties.getProperty("travelingProgress") : 0.0;
	}
	
	@Override
	public void setTravelingProgress(double travelingProgress)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = db.beginTx();
		
		try
		{
			properties.setProperty("travelingProgress", travelingProgress);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public String getInitialDepartureName()
	{
		if (initialDepartureName != null) return initialDepartureName;
		
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return (String) properties.getProperty("initialDepartureName");
	}
	
	@Override
	public Location getDeparture()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		//return Location.valueOf((String) properties.getSingleRelationship(eRelationTypes.UnitDeparture, Direction.OUTGOING).getEndNode().getProperty("location"));		
		return Location.valueOf((String) properties.getProperty("departure"));
	}
	
	@Override
	public void setDeparture(Location departure)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = db.beginTx();
		
		try
		{
			this.departure = departure;
			updateDeparture();
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private void updateDeparture()
	{
		sepDB.getArea(departure);
		
		Relationship oldDeparture = properties.getSingleRelationship(eRelationTypes.UnitDeparture, Direction.OUTGOING);
		if (oldDeparture != null) oldDeparture.delete();		
		
		IndexHits<Node> hits = sepDB.getDB().index().forNodes("AreaIndex").get("location", departure.toString());
		if (!hits.hasNext())
		{
			throw new DBGraphException("Constraint error: Cannot find Area[location='"+departure.toString()+"']. Area must be created before Unit.");				
		}
		
		Node nArea = hits.getSingle();
		
		properties.createRelationshipTo(nArea, eRelationTypes.UnitDeparture);
		properties.setProperty("departure", departure.toString());
	}
	
	@Override
	public Location getDestination()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Relationship r = properties.getSingleRelationship(eRelationTypes.UnitDestination, Direction.OUTGOING);
		
		if (r == null) return null;
		
		return Location.valueOf((String) r.getEndNode().getProperty("location"));
	}
	
	@Override
	public void setDestination(Location destination)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = db.beginTx();
		
		try
		{
			Relationship currentDestination = properties.getSingleRelationship(eRelationTypes.UnitDestination, Direction.OUTGOING);
			
			if (destination == null)
			{
				if (currentDestination != null)
				{
					currentDestination.delete();
				}
			}
			else
			{
				if (currentDestination != null)
				{
					tx.failure();
					throw new RuntimeException("Destination already defined");
				}
				
				sepDB.getArea(destination);
				Node nArea = db.index().forNodes("AreaIndex").get("location", destination.toString()).getSingle();
				
				properties.createRelationshipTo(nArea, eRelationTypes.UnitDestination);							
			}
			
			properties.setProperty("travelingProgress", 0.0D);
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public float getSight()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return sepDB.getConfig().getUnitTypeSight(getType());
	}
	
	@Override
	public void logEncounter(IUnit encounteredUnit, double step)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = db.beginTx();
		
		try
		{		
			// NOTE: We do not delete previous marker versions.		
			IUnitMarker unitMarker = encounteredUnit.getMarker(step);
					
			EncounterLog encounterLog = new EncounterLog(getOwnerName(), getName(), unitMarker.getOwnerName(), unitMarker.getName(), unitMarker.getTurn());
			encounterLog.create(sepDB);
			
			Node nEncounterLog = db.index().forNodes("EncounterLogIndex").get(EncounterLog.PK, EncounterLog.getPK(unitMarker.getTurn(), getOwnerName(), getName(), unitMarker.getOwnerName(), unitMarker.getName())).getSingle();
			
			properties.createRelationshipTo(nEncounterLog, eRelationTypes.UnitEncounterLog);
			
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
			// TODO: implement offline version ?
			sb.append("db offline");
			return sb.toString();
		}
		
		checkForDBUpdate();
		
		sb.append(String.format("[%s] %s\n", getOwnerName(), getName()));		
		
		ICelestialBody dep = sepDB.getArea(getDeparture()).getCelestialBody();
		ICelestialBody dest = getDestination() == null ? null : sepDB.getArea(getDestination()).getCelestialBody();
		
		if (isStopped())
		{
			sb.append(String.format("Stopped on %s\n", dep == null ? getDeparture().toString() : dep.getName()));
		}
		else
		{
			sb.append(String.format("Moving from %s to %s\n", dep == null ? getDeparture().toString() : dep.getName(), dest == null ? getDestination().toString() : dest.getName()));
		}
		
		return sb.toString();
	}
}