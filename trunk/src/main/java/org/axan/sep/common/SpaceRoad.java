/**
 * @author Escallier Pierre
 * @file SpaceRoad.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;

import org.axan.sep.common.SEPUtils.RealLocation;



/**
 * Represent a space road.
 */
public class SpaceRoad implements IMarker, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final int creationDate;
	private final boolean isVisible;
	
	private final String source;
	private final String destination;
	private final int speed;
	
	/**
	 * Full constructor. 
	 */
	public SpaceRoad(int creationDate, boolean isVisible, String source, String destination, int speed)
	{
		this.creationDate = creationDate;
		this.isVisible = isVisible;
		
		this.source = source;
		this.destination = destination;
		this.speed = speed;
	}

	/* (non-Javadoc)
	 * @see common.IMarker#getCreationDate()
	 */
	@Override
	public int getCreationDate()
	{
		return creationDate;
	}

	/* (non-Javadoc)
	 * @see common.IMarker#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return isVisible;
	}
	
	
	
	public String getDestination()
	{
		return destination;
	}

	public String getSource()
	{
		return source;
	}

	public int getSpeed()
	{
		return speed;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return source+" to "+destination+" at speed "+speed;
	}
}
