/**
 * @author Escallier Pierre
 * @file LaunchedPulsarMissile.java
 * @date 3 juin 2009
 */
package common;

/**
 * Represent a launched pulsar missile.
 */
public class LaunchedPulsarMissile extends Unit
{
	/**
	 * Full constructor. 
	 */
	public LaunchedPulsarMissile(boolean isVisible, int lastObservation, String name, Player owner, int[] sourceLocation, int[] destinationLocation, int[] currentEstimatedLocation)
	{
		super(isVisible, lastObservation, name, owner, sourceLocation, destinationLocation, currentEstimatedLocation);
	}
}
