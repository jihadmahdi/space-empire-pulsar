/**
 * @author Escallier Pierre
 * @file Fleet.java
 * @date 3 juin 2009
 */
package common;

import java.util.Map;

/**
 * Represent a fleet.
 */
public class Fleet extends Unit
{
	private final Map<Class<? extends IStarship>, Integer> starships;
	
	/**
	 * Full constructor. 
	 */
	public Fleet(boolean isVisible, int lastObservation, String name, Player owner, int[] sourceLocation, int[] destinationLocation, int[] currentEstimatedLocation, Map<Class<? extends IStarship>, Integer> starships)
	{
		super(isVisible, lastObservation, name, owner, sourceLocation, destinationLocation, currentEstimatedLocation);
		this.starships = starships;
	}
}
