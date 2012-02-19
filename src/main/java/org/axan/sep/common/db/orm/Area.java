package org.axan.sep.common.db.orm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.eplib.utils.Basic;
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
import org.axan.sep.common.db.IUnitMarker;
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

class Area extends AGraphObject<Node> implements IArea, Serializable
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
	 * Serialization fields
	 */
	protected ICelestialBody serializedCelestialBody = null;
	protected Set<IUnitMarker> serializedUnitMarkers = new HashSet<IUnitMarker>();
	
	
	/*
	 * DB connection: DB connection and useful objects (e.g. indexes and nodes).
	 * DB connected and permanent value only. i.e. Indexes and Factories only (no value, no node, no relation).
	 */
	private transient Index<Node> areaIndex;
	private transient Node areasFactory;
	private transient Node sun;
	
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
		if (isDBOnline())
		{
			checkForDBUpdate();
			
			if (properties == null) return null;
			Relationship cb = properties.getSingleRelationship(eRelationTypes.CelestialBody, Direction.OUTGOING);
			String celestialBodyName = cb == null ? null : (String) cb.getEndNode().getProperty("name");
			
			return celestialBodyName == null ? null : sepDB.getCelestialBody(celestialBodyName);
		}
		else
		{
			return serializedCelestialBody;
		}
	}
	
	@Override
	public void update(IArea areaUpdate)
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		Transaction tx = sepDB.getDB().beginTx();
		
		try
		{			
			/*
			 * CHECK(location == areaUpdate.location)
			 * CHECK(isSun == areaUpdate.isSun)
			 * SI celestialBody != null ALORS celestialBody.update(areaUpdate.celestialBody)
			 * POUR CHAQUE UnitMarker unitMarker : areaUpdate.getUnitsMarkers() FAIRE
			 *   SI unitMarker.getTurn() != sepDB.getTurn() CONTINUE // Ce cas ne devrait pas se produire, les markers plus vieux que le tour courant devrait être filtré lors de la sérialization de areaUpdate.
			 *   existingMarker = sepDB.getMarker(um)
			 *   SI existingMarker != null ALORS
			 *   	existingMarker.update(um)
			 *   SINON
			 *   	sepDB.createMarker(um)
			 *   FSI
			 * FPOUR
			 */
			
			if (!getLocation().equals(areaUpdate.getLocation())) throw new RuntimeException("Illegal area update, areas must have the same location.");
			if (isSun() != areaUpdate.isSun()) throw new RuntimeException("Illegal area update, inconsistent isSun state.");
			
			ICelestialBody celestialBodyUpdate = areaUpdate.getCelestialBody();
			ICelestialBody celestialBody = getCelestialBody();
			
			if ((celestialBodyUpdate == null) != (celestialBody == null)) throw new RuntimeException("Illegal area update, inconsistent celestial body state.");		
	
			if (celestialBody != null)
			{
				celestialBody.update(celestialBodyUpdate);
			}
			
			for(IUnitMarker unitMarkerUpdate : areaUpdate.getUnitsMarkers(null))
			{
				if (unitMarkerUpdate.getTurn() != sepDB.getConfig().getTurn()) throw new RuntimeException("Illegal unit marker update, can only update up to date markers");
				IUnitMarker unitMarker = sepDB.getUnitMarker(unitMarkerUpdate.getTurn(), unitMarkerUpdate.getOwnerName(), unitMarkerUpdate.getName(), unitMarkerUpdate.getType());
				if (unitMarker != null) throw new DBGraphException("Illegal unit marker update, up to date marker already exists.");
				
				unitMarker = sepDB.createUnitMarker(unitMarkerUpdate);			
			}
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}		
	}
	
	@Override
	public boolean isVisible(String playerName)
	{
		assertOnlineStatus(true);		
		return isVisible(sepDB, getLocation(), playerName);
	}
	
	/** Key is SEPCommonDB instance hashcode, value is last cached version. */
	private static transient Map<Integer, Integer> cachedVersion = null;
	/** Key is SEPCommonDB instance hashcode, value is cache. */
	private static transient Map<Integer, Map<String, Boolean>> cachedIsVisible = null;
	
	private static boolean isVisible(SEPCommonDB sepDB, Location location, String playerName)
	{
		// Null player name is Global DB
		if (playerName == null) return true;
		
		if (cachedVersion == null) cachedVersion = new HashMap<Integer, Integer>();
		if (cachedIsVisible == null) cachedIsVisible = new HashMap<Integer, Map<String,Boolean>>();
		
		int hash = sepDB.hashCode();
		int cacheVersion = sepDB.getCacheVersion();
		
		if (!cachedVersion.containsKey(hash) || sepDB.needToRefreshCache(cachedVersion.get(hash)))
		{
			// Clear old cache
			cachedIsVisible.put(hash, new HashMap<String, Boolean>());
			cachedVersion.put(hash, cacheVersion);
		}
		
		if (!cachedIsVisible.get(hash).containsKey(location.toString()))
		{
			cachedIsVisible.get(hash).put(location.toString(), uncachedIsVisible(sepDB, location, playerName));			
		}			
		
		return cachedIsVisible.get(hash).get(location.toString());
	}
		
	private static boolean uncachedIsVisible(SEPCommonDB sepDB, final Location location, final String playerName)
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
		for(IFleet fleet : (Set<IFleet>) area.getUnits(eUnitType.Fleet))
		{
			if (playerName.equals(fleet.getOwnerName())) return true;
		}		
		
		// If player has deployed probe to observe this area
		final double probeSight = sepDB.getConfig().getUnitTypeSight(eUnitType.Probe);
		
		IPlayer player = sepDB.getPlayer(playerName);
		for(IProbe probe : (Set<IProbe>) player.getUnits(eUnitType.Probe))
		{
			if (!probe.isDeployed()) continue;
			if (SEPUtils.getDistance(location.asRealLocation(), probe.getRealLocation()) <= probeSight) return true;
		}
				
		return false;
	}
	
	@Override
	public Set<? extends IUnit> getUnits(eUnitType type)
	{
		return getUnits(type, false);
	}
	
	@Override
	public <T extends IUnitMarker> Set<T> getUnitsMarkers(eUnitType type)
	{
		if (isDBOnline())
		{
			return getUnits(type, true);
		}
		else
		{
			if (type == null) return (Set<T>) serializedUnitMarkers;
			
			Set<T> result = new HashSet<T>();
			for(IUnitMarker um : serializedUnitMarkers)
			{
				if (um.getType() == type)
				{
					result.add((T) um);
				}
			}
			return result;
		}
	}
	
	private <T extends IUnitMarker> Set<T> getUnits(eUnitType type, boolean acceptMarkers)
	{
		Set<T> result = new HashSet<T>();
		
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		for(Node n : properties.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, eRelationTypes.UnitMarkerRealLocation, Direction.INCOMING))
		{
			SEPCommonDB.assertProperty(n, "type");
			if (type != null && !type.toString().equals((String) n.getProperty("type"))) continue;
			
			boolean isMarker = n.hasProperty("turn");
			if (!acceptMarkers && isMarker) continue; // Marker
			
			T u;
			if (isMarker)
			{
				u = (T) sepDB.getUnitMarker((Integer) n.getProperty("turn"), (String) n.getProperty("ownerName"), (String) n.getProperty("name"), eUnitType.valueOf((String) n.getProperty("type")));
			}
			else
			{			
				u = (T) sepDB.getUnit((String) n.getProperty("ownerName"), (String) n.getProperty("name"), eUnitType.valueOf((String) n.getProperty("type")));
			}
			
			result.add(u);
		}
		
		return result;
	}

	private static transient Map<String, Integer> lastObservation = null;
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
			// TODO: optimize, cache last observation turn
			int hash = sepDB.hashCode();
			String cacheKey = String.format("%d-%s", hash, getLocation());
			if (lastObservation == null) lastObservation = new HashMap<String, Integer>();
			if (!lastObservation.containsKey(cacheKey))			
			{
				SEPCommonDB pDB = this.sepDB;
				while(pDB.hasPrevious())
				{
					pDB = pDB.previous();
					if (isVisible(pDB, getLocation(), playerName))
					{
						lastObservation.put(cacheKey, pDB.getConfig().getTurn());
						break;
					}
				}
				
				lastObservation.put(cacheKey, -1);
			}
						
			sb.append((lastObservation.get(cacheKey) < 0)?"never been observed":"last observation on turn "+lastObservation.get(cacheKey));
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
		
		Set<? extends IUnitMarker> units = getUnitsMarkers(null);
		if (units != null && !units.isEmpty())
		{
			sb.append("Units :\n");
			for(IUnitMarker u : units)
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
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		isSun = isSun();
		serializedCelestialBody = getCelestialBody();
		if (serializedUnitMarkers.isEmpty())
		{
			for(IUnitMarker um : getUnitsMarkers(null))
			{
				if (IUnit.class.isInstance(um))
				{
					um = ((IUnit) um).getMarker(0);
				}
				
				serializedUnitMarkers.add(um);
			}
		}
		out.defaultWriteObject();
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}

	public static void initializeProperties(Node properties, Location location)
	{
		properties.setProperty("location", location.toString());
	}
}
