package org.axan.sep.common.db.orm;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IAntiProbeMissileMarker;
import org.axan.sep.common.db.IProbeMarker;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

public class ProbeMarker extends UnitMarker implements IProbeMarker, Serializable
{
	/*
	 * PK: inherited
	 */
	
	/*
	 * Off-DB fields.
	 */
	private boolean isDeployed;
	
	/*
	 * DB connection
	 */
	private transient Index<Node> probeMarkerIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public ProbeMarker(int turn, double step, String ownerName, String serieName, int serialNumber, boolean isStopped, RealLocation realLocation, float speed, boolean isDeployed)
	{
		super(turn, step, ownerName, String.format("%s-%d", serieName, serialNumber), isStopped, realLocation, speed);
		this.isDeployed = isDeployed;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param ownerName
	 * @param name
	 */
	public ProbeMarker(SEPCommonDB sepDB, int turn, double step, String ownerName, String name)
	{
		super(sepDB, turn, step, ownerName, name);
		
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
			
			if (probeMarkerIndex.get(PK, getPK(turn, step, ownerName, name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field '"+PK+"' must be unique, probeMarker["+getPK(turn, step, ownerName, name)+"] already exist.");
			}			
			
			properties = sepDB.getDB().createNode();
			ProbeMarker.initializeProperties(properties, turn, step, ownerName, name, isStopped, realLocation, speed, isDeployed);
			probeMarkerIndex.add(properties, PK, getPK(turn, step, ownerName, name));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get(Player.PK, Player.getPK(ownerName)).getSingle();
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
		if (isDBOnline())
		{		
			checkForDBUpdate();		
			return (Boolean) properties.getProperty("isDeployed");
		}
		
		return isDeployed;
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
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (ProbeMarker.class.isInstance(obj)) return false;
		ProbeMarker pm = (ProbeMarker) obj;
		if (!getSerieName().equals(pm.getSerieName())) return false;
		if (getSerialNumber() != pm.getSerialNumber()) return false;
		if (isDeployed() != pm.isDeployed()) return false;
		return true;
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public int hashCode()
	{
		return super.hashCode() + getSerieName().hashCode() + getSerialNumber() + (isDeployed()?1:0);
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		isDeployed = isDeployed();
		out.defaultWriteObject();
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}
	
	public static void initializeProperties(Node properties, int turn, double step, String ownerName, String name, boolean isStopped, RealLocation realLocation, float speed, boolean isDeployed)
	{
		properties.setProperty("turn", turn);
		properties.setProperty("step", step);
		properties.setProperty("ownerName", ownerName);
		properties.setProperty("name", name);
		properties.setProperty("type", eUnitType.Probe.toString());
		properties.setProperty("isStopped", isStopped);		
		properties.setProperty("realLocation", realLocation.toString());
		properties.setProperty("speed", speed);
		properties.setProperty("isDeployed", isDeployed);
	}

}
