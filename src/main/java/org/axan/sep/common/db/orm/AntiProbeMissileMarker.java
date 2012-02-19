package org.axan.sep.common.db.orm;

import java.io.IOException;
import java.io.Serializable;

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
	private transient Index<Node> antiProbeMissileMarkerIndex;
	
	/**
	 * Off-DB constructor.
	 * @param ownerName
	 * @param name
	 */
	public AntiProbeMissileMarker(int turn, String ownerName, String serieName, int serialNumber, boolean isStopped, RealLocation realLocation, float speed, boolean isFired)
	{
		super(turn, ownerName, String.format("%s-%d", serieName, serialNumber), isStopped, realLocation, speed);
		this.isFired = isFired;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param ownerName
	 * @param name
	 */
	public AntiProbeMissileMarker(SEPCommonDB sepDB, int turn, String ownerName, String name)
	{
		super(sepDB, turn, ownerName, name);
		
		// Null values
		this.isFired = false;
	}
	
	@Override
	final protected void checkForDBUpdate()
	{				
		if (!isDBOnline()) return;
		if (isDBOutdated())
		{
			super.checkForDBUpdate();
			antiProbeMissileMarkerIndex = db.index().forNodes("AntiProbeMissileMarkerIndex");			
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
			
			if (antiProbeMissileMarkerIndex.get(PK, getPK(turn, ownerName, name)).hasNext())
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Indexed field '"+PK+"' must be unique, antiProbeMissileMarker["+getPK(turn, ownerName, name)+"] already exist.");
			}			
			
			properties = sepDB.getDB().createNode();
			AntiProbeMissileMarker.initializeProperties(properties, turn, ownerName, name, isStopped, realLocation, speed, isFired);
			antiProbeMissileMarkerIndex.add(properties, PK, getPK(turn, ownerName, name));
			
			Node nOwner = db.index().forNodes("PlayerIndex").get("name", ownerName).getSingle();
			if (nOwner == null)
			{
				tx.failure();
				throw new DBGraphException("Constraint error: Cannot find owner Player '"+ownerName+"'");
			}			
			nOwner.createRelationshipTo(properties, DynamicRelationshipType.withName(eUnitType.AntiProbeMissile + "Marker"));			
			
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
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		isFired = isFired();
		out.defaultWriteObject();
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}
	
	public static void initializeProperties(Node properties, int turn, String ownerName, String name, boolean isStopped, RealLocation realLocation, float speed, boolean isFired)
	{
		properties.setProperty("turn", turn);
		properties.setProperty("ownerName", ownerName);
		properties.setProperty("name", name);
		properties.setProperty("type", eUnitType.AntiProbeMissile.toString());
		properties.setProperty("isStopped", isStopped);		
		properties.setProperty("realLocation", realLocation.toString());
		properties.setProperty("speed", speed);
		properties.setProperty("isFired", isFired);
	}

}
