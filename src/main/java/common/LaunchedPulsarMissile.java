/**
 * @author Escallier Pierre
 * @file LaunchedPulsarMissile.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * Represent a launched pulsar missile.
 */
public class LaunchedPulsarMissile extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	/**
	 * Full constructor. 
	 */
	public LaunchedPulsarMissile(boolean isVisible, int lastObservation, String name, Player owner, int[] sourceLocation, int[] destinationLocation, int[] currentEstimatedLocation)
	{
		super(isVisible, lastObservation, name, owner, sourceLocation, destinationLocation, currentEstimatedLocation);
	}
}