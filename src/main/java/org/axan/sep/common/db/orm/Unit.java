package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.AGraphObject;
import org.axan.eplib.orm.nosql.AVersionedGraphNode;
import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.axan.sep.common.db.orm.base.IBaseUnit;
import org.axan.sep.common.db.orm.base.BaseUnit;
import org.axan.sep.common.db.Events.EncounterLogPublication;
import org.axan.sep.common.db.Events.UpdateArea;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.Events.AGameEvent;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;
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

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;

abstract class Unit extends AVersionedGraphNode<SEPCommonDB> implements IUnit
{	
	protected static final String getPK(String ownerName, String name)
	{
		return String.format("%s@%s", ownerName, name);
	}
	
	/*
	 * PK
	 */
	protected final String ownerName;
	protected final String name;
	
	/*
	 * Off-DB fields.
	 */
	protected final eUnitType type;
	
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
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public Unit(String ownerName, String name, String productiveCelestialBodyName)
	{
		super(getPK(ownerName, name));
		this.ownerName = ownerName;
		this.name = name;
		this.type = eUnitType.valueOf(getClass().getSimpleName());
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
		super(sepDB, getPK(ownerName, name));
		this.ownerName = ownerName;
		this.name = name;
		this.type = eUnitType.valueOf(getClass().getSimpleName());
		
		// Null values
		this.initialDepartureName = null;
	}
	
	/**
	 * If object is DB connected, check for DB update.
	 */
	@Override
	protected void checkForDBUpdate()
	{
		super.checkForDBUpdate();
		
		if (!isDBOnline()) return;
		if (departure == null && initialDepartureName != null)
		{
			ICelestialBody cb = graphDB.getCelestialBody(initialDepartureName);
			if (cb == null || !IProductiveCelestialBody.class.isInstance(cb)) throw new RuntimeException("Unit initial location must be a valid productive celestial body, "+initialDepartureName+" "+(cb == null ? "does not exist" : "is not productive")+".");
			departure = cb.getLocation();				
		}
	}

	@Override
	protected void initializeProperties()
	{
		super.initializeProperties();
		properties.setProperty("ownerName", ownerName);
		properties.setProperty("name", name);
		properties.setProperty("type", type.toString());
		properties.setProperty("initialDepartureName", initialDepartureName);
		properties.setProperty("departure", departure.toString());
	}
	
	/**
	 * Register properties (add Node to indexes and create relationships).
	 * @param properties
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	protected void register(Node properties)
	{
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			super.register(properties);
			
			if (getLastSingleRelationship(eRelationTypes.PlayerUnit, Direction.INCOMING) == null)
			{
				Node nOwner = AGraphObject.get(graphDB.getDB().index().forNodes("PlayerIndex"), ownerName);
				if (nOwner == null)
				{
					tx.failure();
					throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
				}
				nOwner.createRelationshipTo(properties, eRelationTypes.PlayerUnit); // checked
			}
				
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
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			delete();
			/*
			unitIndex.remove(properties);
			properties.getSingleRelationship(eRelationTypes.PlayerUnit, Direction.INCOMING).delete();
			properties.getSingleRelationship(eRelationTypes.UnitDeparture, Direction.OUTGOING).delete();
			properties.getSingleRelationship(eRelationTypes.UnitMarkerRealLocation, Direction.OUTGOING).delete();
			
			Relationship destination = properties.getSingleRelationship(eRelationTypes.UnitDestination, Direction.OUTGOING);
			if (destination != null) destination.delete();
			
			for(Relationship encounterLog : properties.getRelationships(eRelationTypes.UnitEncounterLog, Direction.OUTGOING))
			{
				Node nUnitMarker = encounterLog.getEndNode();
				
				Relationship publication = nUnitMarker.getSingleRelationship(eRelationTypes.PlayerEncounterLog, Direction.INCOMING);
				
				if (publication == null)
				{
					encounterLog.delete();
					// We choose not to delete UnitMarker (should be on GlobalDB only anyway)
				}
			}
			
			properties.delete();
			*/
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
		
		return graphDB.getVersion();
	}
	
	@Override
	public double getStep()
	{
		return 0;
	}
	
	@Override
	public String getOwnerName()
	{
		return ownerName;
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public eUnitType getType()
	{
		return type;
	}
	
	@Override
	public boolean isStopped()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return !hasRelationship(eRelationTypes.UnitDestination, Direction.OUTGOING);
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
		
		return graphDB.getConfig().getUnitTypeSpeed(getType());
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
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			if (getTravelingProgress() != travelingProgress)
			{
				prepareUpdate();
				properties.setProperty("travelingProgress", travelingProgress);
				updateRealLocation();
			}
						
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
	public String getDepartureName()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Node nArea = getLastSingleRelationship(eRelationTypes.UnitDeparture, Direction.OUTGOING).getEndNode();
		Relationship r = AVersionedGraphNode.getLastSingleRelationship(graphDB, nArea, graphDB.getVersion(), eRelationTypes.CelestialBody, Direction.OUTGOING);
		
		if (r == null) return null;
		
		return (String) r.getEndNode().getProperty("name");
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
		
		Transaction tx = graphDB.getDB().beginTx();
		
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
		Relationship oldDeparture = getLastSingleRelationship(eRelationTypes.UnitDeparture, Direction.OUTGOING);
		if (oldDeparture == null || !departure.equals(Location.valueOf((String) oldDeparture.getEndNode().getProperty("location"))))
		{
			prepareUpdate();
			oldDeparture = getLastSingleRelationship(eRelationTypes.UnitDeparture, Direction.OUTGOING);
		}
		
		if (oldDeparture != null) oldDeparture.delete();
		
		graphDB.getArea(departure);
		Node nArea = queryVersion(graphDB.getDB().index().forNodes("AreaIndex"), Area.getPK(departure), graphDB.getVersion());
		if (nArea == null)
		{
			throw new DBGraphException("Constraint error: Cannot find Area[location='"+departure.toString()+"']. Area must be created before Unit.");				
		}
		
		properties.createRelationshipTo(nArea, eRelationTypes.UnitDeparture); // checked
		properties.setProperty("departure", departure.toString());
		
		updateRealLocation();
	}
	
	private void updateRealLocation()
	{
		Relationship oldRealLocation = getLastSingleRelationship(eRelationTypes.UnitMarkerRealLocation, Direction.OUTGOING);
		Location realLocation = getRealLocation().asLocation();
		
		if (oldRealLocation == null || !realLocation.equals(Location.valueOf((String) oldRealLocation.getProperty("location"))))
		{
			prepareUpdate();
			oldRealLocation = getLastSingleRelationship(eRelationTypes.UnitMarkerRealLocation, Direction.OUTGOING);
		}
		
		if (oldRealLocation != null) oldRealLocation.delete();	
		
		graphDB.getArea(realLocation);
		Node nArea = queryVersion(graphDB.getDB().index().forNodes("AreaIndex"), Area.getPK(realLocation), graphDB.getVersion());
		if (nArea == null)
		{
			throw new DBGraphException("Contraint error: Cannot find Area[location='"+realLocation.toString()+"']. Area must be created first.");
		}
		properties.createRelationshipTo(nArea, eRelationTypes.UnitMarkerRealLocation); // checked
	}
	
	@Override
	public String getDestinationName()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Relationship r = getLastSingleRelationship(eRelationTypes.UnitDestination, Direction.OUTGOING);
		
		if (r == null) return null;
		
		Node nArea = r.getEndNode();
		r = AVersionedGraphNode.getLastSingleRelationship(graphDB, nArea, graphDB.getVersion(), eRelationTypes.CelestialBody, Direction.OUTGOING);
		
		if (r == null) return null;
		return (String) r.getEndNode().getProperty("name");
	}
	
	@Override
	public Location getDestination()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Relationship r = getLastSingleRelationship(eRelationTypes.UnitDestination, Direction.OUTGOING);
		
		if (r == null) return null;
		
		SEPCommonDB.assertProperty(r.getEndNode(), "location");
		return Location.valueOf((String) r.getEndNode().getProperty("location"));
	}
	
	@Override
	public void setDestination(Location destination)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{
			Relationship currentDestination = getLastSingleRelationship(eRelationTypes.UnitDestination, Direction.OUTGOING);
			
			if ((currentDestination == null && destination != null) || (destination == null && currentDestination != null) || (!destination.equals(Location.valueOf((String) currentDestination.getEndNode().getProperty("location")))))
			{
				prepareUpdate();
				currentDestination = getLastSingleRelationship(eRelationTypes.UnitDestination, Direction.OUTGOING);
			}
			
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
				
				graphDB.getArea(destination);
				Node nArea = AVersionedGraphNode.queryVersion(graphDB.getDB().index().forNodes("AreaIndex"), Area.getPK(destination), graphDB.getVersion());				
				properties.createRelationshipTo(nArea, eRelationTypes.UnitDestination); // checked					
			}
			
			setTravelingProgress(0.0D);
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
		
		return graphDB.getConfig().getUnitTypeSight(getType());
	}
	
	@Override
	public void logEncounter(IUnitMarker unitMarker)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		assertLastVersion();
		
		Transaction tx = graphDB.getDB().beginTx();
		
		try
		{		
			unitMarker = graphDB.createUnitMarker(unitMarker);
			
			EncounterLog encounterLog = new EncounterLog(getOwnerName(), getName(), unitMarker.getOwnerName(), unitMarker.getName(), unitMarker.getTurn(), unitMarker.getStep());
			encounterLog.create(graphDB);
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public void onArrival(IGameEventExecutor executor)
	{
		if (AGameEvent.isGlobalView(executor))
		{
			assertOnlineStatus(true);
			checkForDBUpdate();
			assertLastVersion();
			
			Transaction tx = graphDB.getDB().beginTx();
			
			try
			{
				for(Relationship r : getLastRelationships(eRelationTypes.UnitEncounterLog, Direction.OUTGOING))
				{
					if (r.hasProperty("published")) continue;
					
					EncounterLog encounterLog = new EncounterLog(graphDB, ownerName, name, (String) r.getProperty("encounterOwnerName"), (String) r.getProperty("encounterName"), (Integer) r.getProperty("turn"), (Double) r.getProperty("step")); 
					IUnitMarker encounterUnitMarker = encounterLog.getEncounter();
					
					EncounterLogPublication encounterLogPublication = new EncounterLogPublication(ownerName, name, encounterUnitMarker);
					executor.onGameEvent(encounterLogPublication, new HashSet<String>(Arrays.asList(ownerName)));
					
					r.setProperty("published", true);
				}
				
				tx.success();
			}
			finally
			{
				tx.finish();
			}
			
			UpdateArea updateArea = new UpdateArea(graphDB.getArea(getDeparture()));
			executor.onGameEvent(updateArea, new HashSet<String>(Arrays.asList(ownerName)));
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
		
		ICelestialBody dep = graphDB.getArea(getDeparture()).getCelestialBody();
		ICelestialBody dest = getDestination() == null ? null : graphDB.getArea(getDestination()).getCelestialBody();
		
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
	
	/**
	 * We assume Unit class and subclasses are not serializable even if AGraphObjet implements Serializable interface.
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		throw new NotSerializableException();
	}
}
