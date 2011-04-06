/**
 * @author Escallier Pierre
 * @file Unit.java
 * @date 3 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.axan.sep.common.ALogEntry;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.SEPUtils.RealLocation;


/**
 * 
 */
abstract class Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	static class Key implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		private final String name;
		private final String ownerName;
		
		public Key(String name, String ownerName)
		{
			this.name = name;
			this.ownerName = ownerName;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getOwnerName()
		{
			return ownerName;
		}
		
		@Override
		public String toString()
		{
			return String.format("%s %s", name, ownerName);
		}
		
		@Override
		public int hashCode()
		{
			return toString().hashCode();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (!Key.class.isInstance(obj)) return false;
			
			Key k = Key.class.cast(obj);
			return hashCode() == k.hashCode();
		}
	}
	
	// DB context
	protected final DataBase db;
	
	// Constants
	private final Key key;
	
	// Variables
	private RealLocation							sourceLocation;

	private RealLocation							destinationLocation;

	private double								travellingProgress = -1;
	
	private final Set<ALogEntry>			travellingLogs;

	// Views
	private final PlayerDatedView<Integer>	playersLastObservation	= new PlayerDatedView<Integer>();
	private final PlayerDatedView<RealLocation> playersCurrentLocationView = new PlayerDatedView<RealLocation>();
	private final PlayerDatedView<Double> playersSpeedView = new PlayerDatedView<Double>();

	public Unit(DataBase db, Key key, RealLocation sourceLocation, Set<ALogEntry> travellingLogs)
	{
		this.db = db;
		this.key = key;
		this.sourceLocation = sourceLocation;
		this.travellingLogs = travellingLogs == null ? new HashSet<ALogEntry>() : travellingLogs;
	}
	
	/**
	 * Full constructor.
	 */
	public Unit(DataBase db, String name, String ownerName, RealLocation sourceLocation, Set<ALogEntry> travellingLogs)
	{
		this(db, new Key(name, ownerName), sourceLocation, travellingLogs);		
	}

	public Key getKey()
	{
		return key;
	}
	
	public String getName()
	{
		return key.getName();
	}
	
	public String getOwnerName()
	{
		return key.getOwnerName();
	}

	protected int getLastObservation(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			playersLastObservation.updateView(playerLogin, date, date);
		}
		return playersLastObservation.getLastValue(playerLogin, -1);
	}

	public RealLocation getSourceLocation()
	{
		return sourceLocation;
	}
	
	protected RealLocation getSourceLocationView(String playerLogin)
	{
		if (playerLogin.equals(getOwnerName()))
		{
			return sourceLocation;
		}
		return null;
	}
	
	public RealLocation getDestinationLocation()
	{
		return destinationLocation;
	}
	
	protected RealLocation getDestinationLocationView(String playerLogin)
	{
		if (playerLogin.equals(getOwnerName()))
		{
			return destinationLocation;
		}
		return null;
	}
	
	public RealLocation getRealLocation()
	{
		if (travellingProgress <= 0 || destinationLocation == null) return sourceLocation;
		if (travellingProgress >= 1) return destinationLocation;
		return SEPUtils.getMobileLocation(sourceLocation, destinationLocation, travellingProgress, true);
	}
	
	protected RealLocation getCurrentLocationView(int date, String playerLogin, boolean isVisible)
	{
		if (playerLogin.equals(getOwnerName()) || isVisible)
		{
			playersCurrentLocationView.updateView(playerLogin, getRealLocation(), date); 
		}
		
		return playersCurrentLocationView.getLastValue(playerLogin, null);
	}
	
	public double getTravellingProgress()
	{
		return travellingProgress;
	}
	
	public double getTravellingProgressView(String playerLogin)
	{
		if (playerLogin.equals(getOwnerName()))
		{
			return travellingProgress;
		}
		else return -1;
	}
	
	public double getSpeedView(int date, String playerLogin, boolean isVisible)
	{
		if (playerLogin.equals(getOwnerName()) || isVisible)
		{
			playersSpeedView.updateView(playerLogin, getSpeed(), date);
		}
		
		return playersSpeedView.getLastValue(playerLogin, null);
	}
	
	protected void setSourceLocation(RealLocation sourceLocation)
	{
		this.sourceLocation = sourceLocation;
	}
	
	protected void setDestinationLocation(RealLocation destinationLocation)
	{
		if (destinationLocation == null) throw new NullPointerException("destinationLocation cannot be null.");
		this.destinationLocation = destinationLocation;
	}
	
	protected void setTravellingProgress(double travellingProgress)
	{
		this.travellingProgress = travellingProgress;
	}
	
	public void addTravellingLogEntry(ALogEntry logEntry)
	{
		ALogEntry.addUpdateLogEntry(travellingLogs, logEntry);		
	}	
	
	public Set<ALogEntry> getTravellingLogs()
	{
		return travellingLogs;
	}
	
	public boolean isMoving()
	{
		if (travellingProgress < 0 || destinationLocation == null || sourceLocation == null) return false;
		return (travellingProgress != 0 && travellingProgress != 1);		
	}
	
	/**
	 * @param date
	 * @param playerLogin
	 * @return
	 */
	abstract public org.axan.sep.common.Unit getPlayerView(int date, String playerLogin, boolean isVisible);
	
	/**
	 * If a previous move order has been registered, let the unit start the move.
	 * This method must return true if the unit require to be considered as moving for its first move turn (not moved yet).
	 */
	public boolean startMove()
	{
		if (!isMoving())
		{	
			setSourceLocation(getRealLocation());
			setTravellingProgress(0);
			
			return true;
		}
		
		return false;
	}
	
	abstract public double getSpeed();

	public void endMove()
	{
		setTravellingProgress(1);
		setSourceLocation(getRealLocation());
		destinationLocation = null;
	}
	
	/*
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
	{
		ois.defaultReadObject();
		if (travellingLogs == null) travellingLogs = new HashSet<ATravellingLogEntry>();
	}
	*/
}
