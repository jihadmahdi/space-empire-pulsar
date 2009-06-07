/**
 * @author Escallier Pierre
 * @file SpaceRoad.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * Represent a space road.
 */
public class SpaceRoad implements IMarker, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final int creationDate;
	private final boolean isVisible;
	
	private final int[] pointALocation;
	private final int[] pointBLocation;
	private final int speed;
	
	/**
	 * Full constructor. 
	 */
	public SpaceRoad(int creationDate, boolean isVisible, int[] pointALocation, int[] pointBLocation, int speed)
	{
		this.creationDate = creationDate;
		this.isVisible = isVisible;
		
		this.pointALocation = pointALocation;
		this.pointBLocation = pointBLocation;
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "["+pointALocation[0]+";"+pointALocation[1]+";"+pointALocation[2]+"] to ["+pointBLocation[0]+";"+pointBLocation[1]+";"+pointBLocation[2]+"] at speed "+speed;
	}
}
