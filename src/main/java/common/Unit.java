/**
 * @author Escallier Pierre
 * @file Unit.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

import common.SEPUtils.Location;

/**
 * Represent a unit (fleet, probe, ...)
 */
public abstract class Unit implements IObservable, IMobile, Serializable
{	
	private static final long	serialVersionUID	= 1L;
	
	private final boolean isVisible;	
	private final int lastObservation;
	
	// Only if visible	
	private final String name;
	private final Player owner;
	
	// Only if owner
	private final Location sourceLocation;
	private final Location destinationLocation;
	private final Location	currentEstimatedLocation;
	
	/**
	 * Full constructor.
	 */
	public Unit(boolean isVisible, int lastObervation, String name, Player owner, Location sourceLocation, Location destinationLocation, Location currentEstimatedLocation)
	{
		this.isVisible = isVisible;
		this.lastObservation = lastObervation;
		this.name = name;
		this.owner = owner;
		this.sourceLocation = sourceLocation;
		this.destinationLocation = destinationLocation;
		this.currentEstimatedLocation = currentEstimatedLocation;
	}
	
	/* (non-Javadoc)
	 * @see common.Observable#getLastObservation()
	 */
	@Override
	public int getLastObservation()
	{
		return lastObservation;
	}
	/* (non-Javadoc)
	 * @see common.Observable#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return isVisible;
	}

	public String getName()
	{
		return name;
	}
	
	public Player getOwner()
	{
		return owner;
	}
	
	/* (non-Javadoc)
	 * @see common.Mobile#getCurrentEstimatedLocation()
	 */
	@Override
	public Location getCurrentEstimatedLocation()
	{
		return currentEstimatedLocation;
	}

	/* (non-Javadoc)
	 * @see common.Mobile#getDestinationLocation()
	 */
	@Override
	public Location getDestinationLocation()
	{
		return destinationLocation;
	}

	/* (non-Javadoc)
	 * @see common.Mobile#getSourceLocation()
	 */
	@Override
	public Location getSourceLocation()
	{
		return sourceLocation;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		if (owner != null) sb.append("["+owner.getName()+"] ");
		sb.append(name);
		
		if (sourceLocation != null && destinationLocation != null)
		{
			sb.append(", moving from "+sourceLocation+" to "+destinationLocation);
		}
		if (currentEstimatedLocation != null)
		{
			sb.append(", current "+(isVisible?"":"estimated ")+"location : "+currentEstimatedLocation);
		}				
		
		return sb.toString();
	}

	private static int generatedNameCount = 0;
	public static String generateName()
	{
		// TODO
		return "TODO"+(++generatedNameCount);
	}
	
	public boolean isMoving()
	{
		if (currentEstimatedLocation == null || sourceLocation == null || destinationLocation == null) return false;
		
		boolean onSource = true;
		boolean onDestination = true;
		
		if (!currentEstimatedLocation.equals(sourceLocation)) onSource = false;
		if (!currentEstimatedLocation.equals(destinationLocation)) onDestination = false;
		
		return !onSource && !onDestination;
	}
}
