/**
 * @author Escallier Pierre
 * @file CarbonCarrier.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

import common.SEPUtils.RealLocation;

/**
 * Represent a carbon carrier
 */
public class CarbonCarrier extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	/**
	 * 
	 */
	public CarbonCarrier(boolean isVisible, int lastObervation, String name, String ownerName, RealLocation sourceLocation, RealLocation destinationLocation, RealLocation currentLocation, double travellingProgress)
	{
		super(isVisible, lastObervation, name, ownerName, sourceLocation, destinationLocation, currentLocation, travellingProgress);
	}
}
