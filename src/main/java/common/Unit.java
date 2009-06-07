/**
 * @author Escallier Pierre
 * @file Unit.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

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
	private final int[] sourceLocation;
	private final int[] destinationLocation;
	private final int[]	currentEstimatedLocation;
	
	/**
	 * Full constructor.
	 */
	public Unit(boolean isVisible, int lastObervation, String name, Player owner, int[] sourceLocation, int[] destinationLocation, int[] currentEstimatedLocation)
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
	public int[] getCurrentEstimatedLocation()
	{
		return currentEstimatedLocation;
	}

	/* (non-Javadoc)
	 * @see common.Mobile#getDestinationLocation()
	 */
	@Override
	public int[] getDestinationLocation()
	{
		return destinationLocation;
	}

	/* (non-Javadoc)
	 * @see common.Mobile#getSourceLocation()
	 */
	@Override
	public int[] getSourceLocation()
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
			sb.append(", moving from ["+sourceLocation[0]+";"+sourceLocation[1]+";"+sourceLocation[2]+"] to ["+destinationLocation[0]+";"+destinationLocation[1]+";"+destinationLocation[2]+"]");
		}
		if (currentEstimatedLocation != null)
		{
			sb.append(", current "+(isVisible?"":"estimated ")+"location : ["+currentEstimatedLocation[0]+";"+currentEstimatedLocation[1]+";"+currentEstimatedLocation[2]+"]");
		}				
		
		return sb.toString();
	}
}
