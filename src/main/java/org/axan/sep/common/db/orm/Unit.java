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

abstract class Unit extends AGraphObject<Node> implements IUnit
{
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
	protected Index<Node> unitIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public Unit(String ownerName, String name, String productiveCelestialBodyName)
	{
		super(String.format("%s@%s", ownerName, name));
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
		super(sepDB, String.format("%s@%s", ownerName, name));
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
	@OverridingMethodsMustInvokeSuper
	protected void checkForDBUpdate()
	{
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			db = sepDB.getDB();
			
			unitIndex = db.index().forNodes("UnitIndex");
			IndexHits<Node> hits = unitIndex.get("ownerName@name", String.format("%s@%s", ownerName, name));
			
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
	@OverridingMethodsMustInvokeSuper
	protected void create(SEPCommonDB sepDB)
	{
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{
			if (unitIndex.get("ownerName@name", String.format("%s@%s", ownerName, name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'ownerName@name' must be unique, unit[ownerName='"+ownerName+"', name='"+name+"'] already exist.");
			}			
						
			unitIndex.add(properties, "ownerName@name", String.format("%s@%s", ownerName, name));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get("name", ownerName).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}
			nOwner.createRelationshipTo(properties, eRelationTypes.PlayerUnit);
			
			// Ensure area creation
			sepDB.getArea(departure);
			
			IndexHits<Node> hits = sepDB.getDB().index().forNodes("AreaIndex").get("location", departure.toString());
			if (!hits.hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find Area[location='"+departure.toString()+"']. Area must be created before Unit.");				
			}
			
			Node nArea = hits.getSingle();
			
			properties.createRelationshipTo(nArea, eRelationTypes.UnitDeparture);
			
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
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
		
		return !properties.hasRelationship(eRelationTypes.UnitDestination, Direction.OUTGOING);
	}
	
	@Override
	public double getTravellingProgress()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return properties.hasProperty("travellingProgress") ? (Double) properties.getProperty("travellingProgress") : 0.0;
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
		
		return Location.valueOf((String) properties.getProperty("departure"));
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
	public RealLocation getRealLocation()
	{
		Location destination = getDestination();
		if (destination == null) return getDeparture().asRealLocation();
		
		double progress = getTravellingProgress();
		if (progress >= 1) throw new RuntimeException("Progress should not be greater or equal to 1");
		return SEPUtils.getMobileLocation(getDeparture().asRealLocation(), destination.asRealLocation(), progress, true);				
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
