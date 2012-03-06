package org.axan.sep.common.db.orm;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnit;
import org.axan.sep.common.db.IUnitMarker;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

abstract class UnitMarker extends AGraphObject<Node> implements IUnitMarker, Serializable
{
	protected static final String PK = "[turn.step] ownerName@name";
	protected static final String getPK(int turn, double step, String ownerName, String name)
	{
		return String.format("[%d.%.2f] %s@%s", turn, step, ownerName, name);
	}
	/*
	protected static final String queryAnyTurnPK(String ownerName, String name)
	{
		return String.format("[*] %s@%s", ownerName, name);
	}
	*/
	
	/*
	 * PK
	 */
	protected final int turn;
	protected final double step;
	protected final String ownerName;
	protected final String name;
	
	/*
	 * Off-DB fields.
	 */
	protected final eUnitType type;
	protected boolean isStopped;
	protected RealLocation realLocation;
	protected float speed;
	
	/*
	 * DB connection
	 */
	protected transient Index<Node> unitMarkerIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public UnitMarker(int turn, double step, String ownerName, String name, boolean isStopped, RealLocation realLocation, float speed)
	{
		super(getPK(turn, step, ownerName, name));
		this.turn = turn;
		this.step = step;
		this.ownerName = ownerName;
		this.name = name;
		this.type = eUnitType.valueOf(getClass().getSimpleName().endsWith("Marker") ? getClass().getSimpleName().substring(0, getClass().getSimpleName().length() - 6) : getClass().getSimpleName());
		this.isStopped = isStopped;
		this.realLocation = realLocation;
		this.speed = speed;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param turn
	 * @param ownerName
	 * @param name
	 */
	public UnitMarker(SEPCommonDB sepDB, int turn, double step, String ownerName, String name)
	{
		super(sepDB, getPK(turn, step, ownerName, name));
		this.turn = turn;
		this.step = step;
		this.ownerName = ownerName;
		this.name = name;
		this.type = eUnitType.valueOf(getClass().getSimpleName().endsWith("Marker") ? getClass().getSimpleName().substring(0, getClass().getSimpleName().length() - 6) : getClass().getSimpleName());
		
		// Null values
		this.isStopped = false;
		this.realLocation = null;
		this.speed = 0F;
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
			
			unitMarkerIndex = db.index().forNodes("UnitMarkerIndex");
			IndexHits<Node> hits = unitMarkerIndex.get(PK, getPK(turn, step, ownerName, name));
			
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
			if (unitMarkerIndex.get(PK, getPK(turn, step, ownerName, name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field '"+PK+"' must be unique, unitMarker["+getPK(turn, step, ownerName, name)+"] already exist.");
			}			
						
			unitMarkerIndex.add(properties, PK, getPK(turn, step, ownerName, name));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get(Player.PK, Player.getPK(ownerName)).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}
			nOwner.createRelationshipTo(properties, eRelationTypes.PlayerUnitMarker);
			
			// Ensure area creation ?						
			sepDB.getArea(realLocation.asLocation());
			IndexHits<Node> hits = sepDB.getDB().index().forNodes("AreaIndex").get(Area.PK, Area.getPK(realLocation.asLocation()));
			if (!hits.hasNext())
			{
				throw new DBGraphException("Contraint error: Cannot find Area[location='"+realLocation.asLocation().toString()+"']. Area must be created first.");
			}
			
			Node nArea = hits.getSingle();		
			properties.createRelationshipTo(nArea, eRelationTypes.UnitMarkerRealLocation);
			
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
		return turn;
	}
	
	@Override
	public double getStep()
	{
		return step;
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
		if (isDBOnline())
		{		
			checkForDBUpdate();
			return (Boolean) properties.getProperty("isStopped");
		}
		else
		{
			return isStopped;
		}
	}

	@Override
	public RealLocation getRealLocation()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();		
			return RealLocation.valueOf((String) properties.getProperty("realLocation"));
		}
		else
		{
			return realLocation;
		}
	}

	@Override
	public float getSpeed()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();		
			return (Float) properties.getProperty("speed");
		}
		else
		{
			return speed;
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
		
		sb.append(String.format("Observed on turn %d\n[%s] %s (%s)\n", getTurn(), getOwnerName(), getName(), isStopped() ? "stopped" : "moving"));
		
		return sb.toString();
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || !UnitMarker.class.isInstance(obj)) return false;
		UnitMarker um = (UnitMarker) obj;		
		if (!getPK(getTurn(), getStep(), getOwnerName(), getName()).equals(getPK(um.getTurn(), um.getStep(), um.getOwnerName(), um.getName()))) return false;
		if (!getType().equals(um.getType())) return false;
		if (isStopped() != um.isStopped()) return false;
		if (!getRealLocation().equals(um.getRealLocation())) return false;
		if (getSpeed() != um.getSpeed()) return false;
		return true;
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public int hashCode()
	{
		return getPK(getTurn(), getStep(), getOwnerName(), getName()).hashCode() + getType().ordinal() + (isStopped()?1:0) + getRealLocation().hashCode() + (int) getSpeed();
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		isStopped = isStopped();
		realLocation = getRealLocation();
		speed = getSpeed();
		out.defaultWriteObject();
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}
}
