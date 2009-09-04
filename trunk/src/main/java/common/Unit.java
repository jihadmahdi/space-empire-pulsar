/**
 * @author Escallier Pierre
 * @file Unit.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

import common.SEPUtils.RealLocation;

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
	private final String ownerName;
	private final RealLocation currentLocation;
	
	// Only if owner
	private final RealLocation sourceLocation;
	private final RealLocation destinationLocation;
	private final double	travellingProgress;
	
	/**
	 * Full constructor.
	 */
	public Unit(boolean isVisible, int lastObervation, String name, String ownerName, RealLocation sourceLocation, RealLocation destinationLocation, RealLocation currentLocation, double travellingProgress)
	{
		this.isVisible = isVisible;
		this.lastObservation = lastObervation;
		this.name = name;
		this.ownerName = ownerName;
		this.sourceLocation = sourceLocation;
		this.destinationLocation = destinationLocation;
		this.currentLocation = currentLocation;
		this.travellingProgress = travellingProgress;
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
	
	public String getOwnerName()
	{
		return ownerName;
	}
	
	/* (non-Javadoc)
	 * @see common.Mobile#getTravellingProgress()
	 */
	@Override
	public double getTravellingProgress()
	{
		return travellingProgress;
	}

	/* (non-Javadoc)
	 * @see common.Mobile#getDestinationLocation()
	 */
	@Override
	public RealLocation getDestinationLocation()
	{
		return destinationLocation;
	}

	/* (non-Javadoc)
	 * @see common.Mobile#getSourceLocation()
	 */
	@Override
	public RealLocation getSourceLocation()
	{
		return sourceLocation;
	}
	
	/* (non-Javadoc)
	 * @see common.Mobile#getCurrentLocation()
	 */
	@Override
	public RealLocation getCurrentLocation()
	{
		return currentLocation;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		if (ownerName != null) sb.append("["+ownerName+"] ");
		sb.append(name);
		
		if (sourceLocation != null && destinationLocation != null)
		{
			sb.append(", moving from "+sourceLocation+" to "+destinationLocation);
		}
		if (travellingProgress >= 0)
		{
			sb.append(", "+(isVisible?"current estimated progress":"last seen progress")+" : "+travellingProgress);
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
		if (travellingProgress < 0 || sourceLocation == null || destinationLocation == null) return false;
		
		return (travellingProgress != 0 && travellingProgress != 1);		
	}
}
