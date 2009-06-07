/**
 * @author Escallier Pierre
 * @file Unit.java
 * @date 3 juin 2009
 */
package server.model;

import java.util.Hashtable;
import java.util.Map;

import common.EstimatedPulsarMarker;
import common.Player;

/**
 * 
 */
abstract class Unit
{
	// Constants
	private final String					name;

	private final Player					owner;

	// Variables
	private int[]							sourceLocation;

	private int[]							destinationLocation;

	private int[]							currentEstimatedLocation;

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

	public int[] getSourceLocation()
	{
		return sourceLocation;
	}
	
	protected int[] getSourceLocationView(String playerLogin)
	{
		if (owner.isNamed(playerLogin))
		{
			return sourceLocation;
		}
		return null;
	}
	
	public int[] getDestinationLocation()
	{
		return destinationLocation;
	}
	
	protected int[] getDestinationLocationView(String playerLogin)
	{
		if (owner.isNamed(playerLogin))
		{
			return destinationLocation;
		}
		return null;
	}
	
	public int[] getCurrentEstimatedLocation()
	{
		return currentEstimatedLocation;
	}
	
	protected int[] getCurrentEstimatedLocationView(String playerLogin)
	{
		if (owner.isNamed(playerLogin))
		{
			return currentEstimatedLocation;
		}
		return null;
	}
	
	/**
	 * @param date
	 * @param playerLogin
	 * @return
	 */
	abstract public common.Unit getPlayerView(int date, String playerLogin, boolean isVisible);
}
