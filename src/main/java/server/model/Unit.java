/**
 * @author Escallier Pierre
 * @file Unit.java
 * @date 3 juin 2009
 */
package server.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import common.ATravellingLogEntry;
import common.EstimatedPulsarMarker;
import common.Player;
import common.SEPUtils.Location;

/**
 * 
 */
abstract class Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final String					name;

	private final Player					owner;

	// Variables
	private Location							sourceLocation;

	private Location							destinationLocation;

	private Location							currentEstimatedLocation;
	
	private Stack<ATravellingLogEntry>			travellingLog;

	// Views
	private final PlayerDatedView<Integer>	playersLastObservation	= new PlayerDatedView<Integer>();

	/**
	 * Full constructor.
	 */
	public Unit(String name, Player owner)
	{
		this.name = name;
		this.owner = owner;
	}

	/**
	 * @return owner.
	 */
	public Player getOwner()
	{
		return owner;
	}

	public String getName()
	{
		return name;
	}

	protected int getLastObservation(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			playersLastObservation.updateView(playerLogin, date, date);
		}
		return playersLastObservation.getLastValue(playerLogin, -1);
	}

	public Location getSourceLocation()
	{
		return sourceLocation;
	}
	
	protected Location getSourceLocationView(String playerLogin)
	{
		if (owner.isNamed(playerLogin))
		{
			return sourceLocation;
		}
		return null;
	}
	
	public Location getDestinationLocation()
	{
		return destinationLocation;
	}
	
	protected Location getDestinationLocationView(String playerLogin)
	{
		if (owner.isNamed(playerLogin))
		{
			return destinationLocation;
		}
		return null;
	}
	
	public Location getCurrentEstimatedLocation()
	{
		return currentEstimatedLocation;
	}
	
	protected Location getCurrentEstimatedLocationView(String playerLogin)
	{
		if (owner.isNamed(playerLogin))
		{
			return currentEstimatedLocation;
		}
		return null;
	}
	
	protected void setSourceLocation(Location sourceLocation)
	{
		this.sourceLocation = sourceLocation;
	}
	
	protected void setDestinationLocation(Location destinationLocation)
	{
		this.destinationLocation = destinationLocation;
	}
	
	protected void setCurrentLocation(Location currentLocation)
	{
		this.currentEstimatedLocation = currentLocation;
	}
	
	public void addTravelligLogEntry(ATravellingLogEntry logEntry)
	{
		if (travellingLog == null)
		{
			travellingLog = new Stack<ATravellingLogEntry>();
		}
		
		travellingLog.push(logEntry);
	}
	
	public boolean isMoving()
	{
		if (currentEstimatedLocation == null || destinationLocation == null || sourceLocation == null) return false;
		return (!currentEstimatedLocation.equals(sourceLocation) && !currentEstimatedLocation.equals(destinationLocation));
	}
	
	/**
	 * @param date
	 * @param playerLogin
	 * @return
	 */
	abstract public common.Unit getPlayerView(int date, String playerLogin, boolean isVisible);
	
	/**
	 * If a previous move order has been registered, let the unit start the move.
	 * This method must return true if the unit require to be considered as moving for its first move turn (not moved yet).
	 */
	abstract public boolean startMove(Location currentLocation, GameBoard gameBoard);	
	
	abstract public double getSpeed();

	abstract public void endMove(Location currentLocation, GameBoard gameBoard);

	public String getOwnerName()
	{
		return (owner == null?null:owner.getName());
	}
}
