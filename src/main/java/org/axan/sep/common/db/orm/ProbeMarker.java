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
		super.checkForDBUpdate();
	}
	
	@Override
	final protected void initializeProperties()
	{
		super.initializeProperties();
		properties.setProperty("isDeployed", isDeployed);
	}
	
	/**
	 * Register properties (add Node to indexes and create relationships).
	 * @param properties
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	final protected void register(Node properties)
	{
		super.register(properties);
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

}
