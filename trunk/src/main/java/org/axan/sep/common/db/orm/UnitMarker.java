package org.axan.sep.common.db.orm;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.ICelestialBody;
import org.axan.sep.common.db.IProductiveCelestialBody;
import org.axan.sep.common.db.IUnitMarker;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

abstract class UnitMarker extends AGraphObject<Node> implements IUnitMarker
{
	protected static final String PK = "[turn] ownerName@name";
	protected static final String getPK(int turn, String ownerName, String name)
	{
		return String.format("[%d] %s@%s", turn, ownerName, name);
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
	protected final String ownerName;
	protected final String name;
	
	/*
	 * Off-DB fields.
	 */
	protected final eUnitType type;
	protected final boolean isStopped;
	protected final RealLocation realLocation;
	protected final float speed;
	
	/*
	 * DB connection
	 */
	protected Index<Node> unitMarkerIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public UnitMarker(int turn, String ownerName, String name, boolean isStopped, RealLocation realLocation, float speed)
	{
		super(getPK(turn, ownerName, name));
		this.turn = turn;
		this.ownerName = ownerName;
		this.name = name;
		this.type = eUnitType.valueOf(getClass().getSimpleName());
		this.isStopped = isStopped;
		this.realLocation = realLocation;
		this.speed = speed;
	}
	
	public UnitMarker(SEPCommonDB sepDB, int turn, String ownerName, String name)
	{
		super(sepDB, getPK(turn, ownerName, name));
		this.turn = turn;
		this.ownerName = ownerName;
		this.name = name;
		this.type = eUnitType.valueOf(getClass().getSimpleName());
		
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
			IndexHits<Node> hits = unitMarkerIndex.get(PK, getPK(turn, ownerName, name));
			
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
			if (unitMarkerIndex.get(PK, getPK(turn, ownerName, name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field '"+PK+"' must be unique, unitMarker["+getPK(turn, ownerName, name)+"] already exist.");
			}			
						
			unitMarkerIndex.add(properties, PK, getPK(turn, ownerName, name));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get("name", ownerName).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}
			nOwner.createRelationshipTo(properties, eRelationTypes.PlayerUnitMarker);
			
			// Ensure area creation ?			
			
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
		
		return (Boolean) properties.getProperty("isStopped");
	}

	@Override
	public RealLocation getRealLocation()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return RealLocation.valueOf((String) properties.getProperty("realLocation"));
	}

	@Override
	public float getSpeed()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return (Float) properties.getProperty("speed");
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
}
