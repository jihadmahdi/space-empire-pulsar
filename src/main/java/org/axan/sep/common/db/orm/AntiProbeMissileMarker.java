package org.axan.sep.common.db.orm;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.axan.eplib.orm.nosql.DBGraphException;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IAntiProbeMissile;
import org.axan.sep.common.db.IAntiProbeMissileMarker;
import org.axan.sep.common.db.IProbe;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

public class AntiProbeMissileMarker extends UnitMarker implements IAntiProbeMissileMarker, Serializable
{
	/*
	 * PK: inherited
	 */
	
	/*
	 * Off-DB fields.
	 */
	private boolean isFired;
	
	/*
	 * DB connection
	 */
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public AntiProbeMissileMarker(int turn, double step, String ownerName, String serieName, int serialNumber, boolean isStopped, RealLocation realLocation, float speed, boolean isFired)
	{
		super(turn, step, ownerName, String.format("%s-%d", serieName, serialNumber), isStopped, realLocation, speed);
		this.isFired = isFired;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param ownerName
	 * @param name
	 */
	public AntiProbeMissileMarker(SEPCommonDB sepDB, int turn, double step, String ownerName, String name)
	{
		super(sepDB, turn, step, ownerName, name);
		
		// Null values
		this.isFired = false;
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
		properties.setProperty("isFired", isFired);
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
	public boolean isFired()
	{
		if (isDBOnline())
		{
			checkForDBUpdate();		
			return (Boolean) properties.getProperty("isFired");
		}
		else
		{
			return isFired;
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());		
		if (!isDBOnline()) return sb.toString();
		
		checkForDBUpdate();

		sb.append("Status : "+(isFired() ? "fired" : "not fired"));
		return sb.toString();
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;		
		if (AntiProbeMissileMarker.class.isInstance(obj)) return false;
		AntiProbeMissileMarker apmm = (AntiProbeMissileMarker) obj;
		if (!getSerieName().equals(apmm.getSerieName())) return false;
		if (getSerialNumber() != apmm.getSerialNumber()) return false;
		if (isFired() != apmm.isFired()) return false;
		return true;
	}
	
	@Override
	@OverridingMethodsMustInvokeSuper
	public int hashCode()
	{
		return super.hashCode() + getSerieName().hashCode() + getSerialNumber() + (isFired()?1:0);
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		isFired = isFired();
		out.defaultWriteObject();
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}
}
