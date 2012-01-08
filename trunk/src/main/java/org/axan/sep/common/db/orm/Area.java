package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.Rules;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IPlayer;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

class Area extends AGraphObject<Node> implements IArea
{
	/*
	 * PK: first pk field.
	 */
	protected final Location location;
	
	/*
	 * Off-DB fields (none)
	 */
	protected boolean isSun;
	
	/*
	 * DB connection: DB connection and useful objects (e.g. indexes and nodes).
	 * DB connected and permanent value only. i.e. Indexes and Factories only (no value, no node, no relation).
	 */
	private Index<Node> areaIndex;
	private Node areasFactory;
	private Node sun;
	
	/**
	 * Off-DB constructor.
	 * @param location
	 * @param isSun
	 */
	public Area(Location location)
	{
		super(location);
		this.location = location;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param location
	 */
	public Area(SEPCommonDB sepDB, Location location)
	{
		super(sepDB, location);
		this.location = location;		
	}
	
	/**
	 * If object is DB connected, check for DB update.
	 */
	@Override
	final protected void checkForDBUpdate()
	{
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			db = sepDB.getDB();
			
			Location sunLocation = Rules.getSunLocation(sepDB.getConfig());
			isSun = (SEPUtils.getDistance(sunLocation, location) <= sepDB.getConfig().getSunRadius());
			areaIndex = db.index().forNodes("AreaIndex");
			areasFactory = db.getReferenceNode().getSingleRelationship(eRelationTypes.Areas, Direction.OUTGOING).getEndNode();
			sun = db.getReferenceNode().getSingleRelationship(eRelationTypes.Sun, Direction.OUTGOING).getEndNode();
			IndexHits<Node> hits = areaIndex.get("location", location.toString());
			properties = hits.hasNext() ? hits.getSingle() : null;			
		}
	}
	
	/**
	 * Current object must be Off-DB to call create method.
	 * Create method connect the object to the given DB and create the object node.
	 * After this call, object is DB connected.
	 * @param sepDB
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
			
			if (areaIndex.get("location", location.toString()).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field 'location' must be unique, area[location='"+location+"'] already exist.");
			}
			properties = sepDB.getDB().createNode();
			Area.initializeProperties(properties, location);			
			areasFactory.createRelationshipTo(properties, eRelationTypes.Areas);
			if (isSun)
			{
				sun.createRelationshipTo(properties, eRelationTypes.Sun);
			}
			areaIndex.add(properties, "location", location.toString());
						
			tx.success();			
		}
		finally
		{
			tx.finish();
		}
	}	

	@Override
	public Location getLocation()
	{
		return location;
	}	

	@Override
	public boolean isSun()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();
			if (properties == null) return false;
			return properties.hasRelationship(eRelationTypes.Sun, Direction.INCOMING);
		}
		else
		{
			return isSun;
		}		
	}
	
	@Override
	public ICelestialBody getCelestialBody()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		if (properties == null) return null;
		Relationship cb = properties.getSingleRelationship(eRelationTypes.CelestialBody, Direction.OUTGOING);
		String celestialBodyName = cb == null ? null : (String) cb.getEndNode().getProperty("name");
		
		return celestialBodyName == null ? null : sepDB.getCelestialBody(celestialBodyName);
	}
	
	@Override
	public boolean isVisible(String playerName)
	{
		assertOnlineStatus(true);
		return isVisible(sepDB, getLocation(), playerName);
	}
	
	private static boolean isVisible(SEPCommonDB sepDB, final Location location, final String playerName)
	{
		IArea area = sepDB.getArea(location);
		
		// TODO: Pulsar effect is not implemented yet
		
		ICelestialBody cb = area.getCelestialBody();
		IProductiveCelestialBody pcb = IProductiveCelestialBody.class.isInstance(cb) ? (IProductiveCelestialBody) cb : null;
		
		// If player own a (productive) celestial body in this area		
		if (pcb != null && playerName.equals(pcb.getOwner())) return true;		
		
		// If player has assigned fleet in this area
		IFleet assignedFleet = (pcb == null) ? null : pcb.getAssignedFleet(playerName);
		if (assignedFleet != null) return true;
		
		// If player has stopped fleet in this area
		for(IFleet fleet : area.getUnits(IFleet.class))
		{
			if (playerName.equals(fleet.getOwnerName())) return true;
		}		
		
		// If player has deployed probe to observe this area
		final double probeSight = sepDB.getConfig().getUnitTypeSight(eUnitType.Probe);
		
		IPlayer player = sepDB.getPlayer(playerName);
		for(IProbe probe : player.getUnits(IProbe.class))
		{
			if (!probe.isDeployed()) continue;
			if (SEPUtils.getDistance(location.asRealLocation(), probe.getRealLocation()) <= probeSight) return true;
		}
				
		return false;
	}
	
	@Override
	public <T extends IUnit> Set<T> getUnits(final Class<T> expectedType)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Set<T> result = new HashSet<T>();
		
		eUnitType type;
		if (IUnit.class.equals(expectedType))
		{
			type = null;
		}
		else try
		{
			type = eUnitType.valueOf(expectedType.getSimpleName().charAt(0) == 'I' ? expectedType.getSimpleName().substring(1) : expectedType.getSimpleName());
		}
		catch(IllegalArgumentException e)
		{
			throw new RuntimeException("Unknown eUnitType value for class '"+expectedType.getSimpleName()+"'", e);
		}
		
		for(Node n : properties.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eRelationTypes.UnitDeparture, Direction.INCOMING))
		{
			if (type != null)
			{
				if (!type.toString().equals((String) n.getProperty("type"))) continue;
			}
			else
			{
				type = eUnitType.valueOf((String) n.getProperty("type"));
			}			
			
			T unit = (T) sepDB.getUnit((String) n.getProperty("ownerName"), (String) n.getProperty("name"), type);
			if (unit.isStopped()) result.add(unit);
		}
		
		return result;
	}

	public String toString(String playerName)
	{
		StringBuilder sb = new StringBuilder();
		
		if (!isDBOnline())
		{
			// TODO: implement offline version ?
			sb.append("db offline");
			return sb.toString();
		}
		
		checkForDBUpdate();
		if (isVisible(playerName))
		{
			sb.append("currently observed");
		}
		else
		{
			int lastObservation = -1;
			SEPCommonDB pDB = this.sepDB;
			while(pDB.hasPrevious())
			{
				pDB = pDB.previous();
				if (isVisible(pDB, getLocation(), playerName))
				{
					lastObservation = pDB.getConfig().getTurn();
					break;
				}
			}
			
			sb.append((lastObservation < 0)?"never been observed":"last observation on turn "+lastObservation);
		}
		sb.append("\n");
		
		if (isSun())
		{
			sb.append("Sun\n");
		}
		else
		{
			ICelestialBody cb = getCelestialBody();
			if (cb != null)
			{
				sb.append(cb.toString()+"\n");
			}
		}
		
		Set<IUnit> units = getUnits(IUnit.class);
		if (units != null && !units.isEmpty())
		{
			sb.append("Units :\n");
			for(IUnit u : units)
			{
				sb.append("   ["+u.getOwnerName()+"] "+u.getName()+"\n");
			}
		}
		
		/*
		if (markers != null && !markers.isEmpty())
		{
			sb.append("Markers :\n");
			for(IMarker m : markers)
			{
				sb.append(m);
			}
		}
		*/
		
		return sb.toString();
	}

	public static void initializeProperties(Node properties, Location location)
	{
		properties.setProperty("location", location.toString());
	}
}
