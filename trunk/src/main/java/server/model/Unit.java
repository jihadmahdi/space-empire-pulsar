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
import common.SEPUtils;
import common.SEPUtils.RealLocation;

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
	private RealLocation							sourceLocation;

	private RealLocation							destinationLocation;

	private double								travellingProgress = -1;
	
	private Stack<ATravellingLogEntry>			travellingLog;

	// Views
	private final PlayerDatedView<Integer>	playersLastObservation	= new PlayerDatedView<Integer>();
	private final PlayerDatedView<RealLocation> playersCurrentLocationView = new PlayerDatedView<RealLocation>();

	/**
	 * Full constructor.
	 */
	public Unit(String name, Player owner, RealLocation sourceLocation)
	{
		this.name = name;
		this.owner = owner;
		this.sourceLocation = sourceLocation;
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

	public RealLocation getSourceLocation()
	{
		return sourceLocation;
	}
	
	protected RealLocation getSourceLocationView(String playerLogin)
	{
		if (owner.isNamed(playerLogin))
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
		if (owner.isNamed(playerLogin))
		{
			return destinationLocation;
		}
		return null;
	}
	
	public RealLocation getCurrentLocation()
	{
		if (travellingProgress < 0 || destinationLocation == null) return sourceLocation;
		if (travellingProgress == 0) return sourceLocation;
		if (travellingProgress == 1) return destinationLocation;
		return SEPUtils.getMobileLocation(sourceLocation, destinationLocation, travellingProgress, true);
	}
	
	protected RealLocation getCurrentLocationView(int date, String playerLogin, boolean isVisible)
	{
		if (owner.isNamed(playerLogin) || isVisible)
		{
			playersCurrentLocationView.updateView(playerLogin, getCurrentLocation(), date); 
		}
		
		return playersCurrentLocationView.getLastValue(playerLogin, null);
	}
	
	public double getTravellingProgress()
	{
		return travellingProgress;
	}
	
	public double getTravellingProgressView(String playerLogin)
	{
		if (owner != null && owner.isNamed(playerLogin))
		{
			return travellingProgress;
		}
		else return -1;
	}		
	
	protected void setSourceLocation(RealLocation sourceLocation)
	{
		this.sourceLocation = sourceLocation;
	}
	
	protected void setDestinationLocation(RealLocation destinationLocation)
	{
		this.destinationLocation = destinationLocation;
	}
	
	protected void setTravellingProgress(double travellingProgress)
	{
		this.travellingProgress = travellingProgress;
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
		if (travellingProgress < 0 || destinationLocation == null || sourceLocation == null) return false;
		return (travellingProgress != 0 && travellingProgress != 1);		
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
	public boolean startMove(RealLocation currentLocation, GameBoard gameBoard)
	{
		if (!isMoving())
		{	
			setSourceLocation(currentLocation);
			setTravellingProgress(0);
			
			return true;
		}
		
		return false;
	}
	
	abstract public double getSpeed();

	public void endMove(RealLocation currentLocation, GameBoard gameBoard)
	{
		setSourceLocation(currentLocation);
		setTravellingProgress(1);
	}

	public String getOwnerName()
	{
		return (owner == null?null:owner.getName());
	}	
}
