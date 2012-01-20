package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IProbeMarker;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

public class ProbeMarker extends UnitMarker implements IProbeMarker
{
	/*
	 * PK: inherited
	 */
	
	/*
	 * Off-DB fields.
	 */
	private final boolean isDeployed;
	
	/*
	 * DB connection
	 */
	private transient Index<Node> probeMarkerIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public ProbeMarker(int turn, String ownerName, String serieName, int serialNumber, boolean isStopped, RealLocation realLocation, float speed, boolean isDeployed)
	{
		super(turn, ownerName, String.format("%s-%d", serieName, serialNumber), isStopped, realLocation, speed);
		this.isDeployed = isDeployed;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param ownerName
	 * @param name
	 */
	public ProbeMarker(SEPCommonDB sepDB, int turn, String ownerName, String name)
	{
		super(sepDB, turn, ownerName, name);
		
		// Null values
		this.isDeployed = false;
	}
	
	@Override
	final protected void checkForDBUpdate()
	{				
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			super.checkForDBUpdate();
			probeMarkerIndex = db.index().forNodes("ProbeMarkerIndex");			
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
			
			if (probeMarkerIndex.get(PK, getPK(turn, ownerName, name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field '"+PK+"' must be unique, probeMarker["+getPK(turn, ownerName, name)+"] already exist.");
			}			
			
			properties = sepDB.getDB().createNode();
			ProbeMarker.initializeProperties(properties, turn, ownerName, name, isStopped, realLocation, speed, isDeployed);
			probeMarkerIndex.add(properties, PK, getPK(turn, ownerName, name));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get("name", ownerName).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}			
			nOwner.createRelationshipTo(properties, DynamicRelationshipType.withName(eUnitType.Probe + "Marker"));			
			
			super.create(sepDB);
			
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
	public boolean isDeployed()
	{
		assertOnlineStatus(true);
		checkForDBUpdate();
		
		return (Boolean) properties.getProperty("isDeployed");
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());		
		if (!isDBOnline()) return sb.toString();
		
		checkForDBUpdate();
		
		sb.append("Status : "+(isDeployed()?"deployed":"not deployed"));
		return sb.toString();
	}
	
	public static void initializeProperties(Node properties, int turn, String ownerName, String name, boolean isStopped, RealLocation realLocation, float speed, boolean isDeployed)
	{
		properties.setProperty("turn", turn);
		properties.setProperty("ownerName", ownerName);
		properties.setProperty("name", name);
		properties.setProperty("type", eUnitType.AntiProbeMissile.toString());
		properties.setProperty("isStopped", isStopped);		
		properties.setProperty("realLocation", realLocation.toString());
		properties.setProperty("speed", speed);
		properties.setProperty("isDeployed", isDeployed);
	}

}
