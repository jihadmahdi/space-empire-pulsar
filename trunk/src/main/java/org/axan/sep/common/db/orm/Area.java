package org.axan.sep.common.db.orm;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IArea;
import org.axan.sep.common.db.ICelestialBody;
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

class Area extends AGraphObject implements IArea
{
	/*
	 * PK: first pk field.
	 */
	protected final Location location;
	
	/*
	 * Off-DB fields (none)
	 */
	protected final boolean isSun;
	
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
	public Area(Location location, boolean isSun)
	{
		super(location);
		this.location = location;
		this.isSun = isSun;
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
		
		// Null values
		this.isSun = false;
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
			
			areaIndex = db.index().forNodes("AreaIndex");
			areasFactory = db.getReferenceNode().getSingleRelationship(eRelationTypes.Areas, Direction.OUTGOING).getEndNode();
			sun = db.getReferenceNode().getSingleRelationship(eRelationTypes.Sun, Direction.OUTGOING).getEndNode();
			IndexHits<Node> hits = areaIndex.get("location", location.toString());
			node = hits.hasNext() ? hits.getSingle() : null;			
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
			node = sepDB.getDB().createNode();
			Area.initializeNode(node, location);			
			areasFactory.createRelationshipTo(node, eRelationTypes.Areas);
			if (isSun)
			{
				sun.createRelationshipTo(node, eRelationTypes.Sun);
			}
			areaIndex.add(node, "location", location.toString());
						
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
			return node.hasRelationship(eRelationTypes.Sun, Direction.INCOMING);
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
		
		Relationship cb = node.getSingleRelationship(eRelationTypes.CelestialBody, Direction.OUTGOING);
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
		Area area = new Area(sepDB, location);
		
		// TODO: Pulsar effect is not implemented yet
		
		// If player own a (productive) celestial body in this area
		ICelestialBody cb = area.getCelestialBody();
				
		if (cb != null && IProductiveCelestialBody.class.isInstance(cb) && playerName.equals(IProductiveCelestialBody.class.cast(cb).getOwner())) return true;		
		
		// If player has assigned fleet in this area
		if (cb != null && area.node.getSingleRelationship(eRelationTypes.CelestialBody, Direction.OUTGOING).getEndNode().traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator()
		{
			
			@Override
			public boolean isReturnableNode(TraversalPosition currentPos)
			{
				Node n = currentPos.currentNode();
				return (n.hasRelationship(eRelationTypes.AssignedCelestialBody, Direction.OUTGOING) && playerName.equals(n.getProperty("owner")));
			}
		}, eRelationTypes.AssignedCelestialBody, Direction.INCOMING).iterator().hasNext()) return true;
		
		// If player has stopped fleet in this area
		if (area.node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator()
		{			
			@Override
			public boolean isReturnableNode(TraversalPosition currentPos)
			{
				Node n = currentPos.currentNode();
				return (n.hasRelationship(eUnitType.Fleet, Direction.OUTGOING) && playerName.equals(n.getProperty("owner")) && (false == ((((Integer) n.getProperty("progress")) > 0) && (((Integer) n.getProperty("progress")) != 1) && (n.getProperty("destination_x") != null) && (n.getProperty("departure_x") != null))) );
			}
		}, eUnitType.Fleet, Direction.INCOMING).iterator().hasNext()) return true;
		
		// If player has deployed probe to observe this area
		final double probeSight = sepDB.getConfig().getUnitTypeSight(eUnitType.Probe);
		if (area.node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator()
		{
			
			@Override
			public boolean isReturnableNode(TraversalPosition currentPos)
			{
				if (currentPos.isStartNode()) return false;
				
				Node n = currentPos.currentNode();
				double distance = Math.pow(location.x - (Integer) n.getProperty("departure_x"), 2) + Math.pow(location.y - (Integer) n.getProperty("departure_y"), 2) + Math.pow(location.z - (Integer) n.getProperty("departure_z"), 2);
				return (n.hasRelationship(eUnitType.Probe, Direction.OUTGOING) && playerName.equals(n.getProperty("owner")) && ((Integer) n.getProperty("progress") == 1) && distance <= probeSight);
			}
		}, eUnitType.Probe, Direction.INCOMING).iterator().hasNext()) return true;
		
		return false;
	}
	
	@Override
	public <T extends IUnit> Set<T> getUnits(final Class<T> expectedType)
	{
		try
		{
			Set<T> result = new HashSet<T>();
			
			/*
			// TODO
			eUnitType type;
			try
			{
				type = eUnitType.valueOf(expectedType.getSimpleName().charAt(0) == 'I' ? expectedType.getSimpleName().substring(1) : expectedType.getSimpleName());
			}
			catch(IllegalArgumentException e)
			{
				type = null;
			}
			
			for(Node n : node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, type == null ? eRelationsTypes.Unit : type, Direction.INCOMING))
			{
				result.add(DataBaseORMGenerator.mapTo(expectedType, n));
			}
			*/
			return result;
		}
		catch(Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	public String toString(String playerName)
	{
		StringBuffer sb = new StringBuffer();
		
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
				sb.append("   ["+u.getOwner()+"] "+u.getName()+"\n");
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

	public static void initializeNode(Node node, Location location)
	{
		node.setProperty("location", location.toString());
	}
}
