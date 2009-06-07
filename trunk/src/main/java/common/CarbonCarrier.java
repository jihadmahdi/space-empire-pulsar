/**
 * @author Escallier Pierre
 * @file CarbonCarrier.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * Represent a carbon carrier
 */
public class CarbonCarrier extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	/**
	 * 
	 */
	public CarbonCarrier(boolean isVisible, int lastObervation, String name, Player owner, int[] sourceLocation, int[] destinationLocation, int[] currentEstimatedLocation)
	{
		super(isVisible, lastObervation, name, owner, sourceLocation, destinationLocation, currentEstimatedLocation);
	}
}
