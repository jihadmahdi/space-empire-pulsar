/**
 * @author Escallier Pierre
 * @file FilteredAsteroidField.java
 * @date 3 juin 2009
 */
package common;

import java.util.Set;

/**
 * 
 */
public class AsteroidField extends ProductiveCelestialBody
{
	public static final int		CARBON_MIN		= 60 * 1000;

	public static final int		CARBON_MAX		= 300 * 1000;

	public static final int		SLOTS_MIN		= 3;

	public static final int		SLOTS_MAX		= 6;

	public static final float	GENERATION_RATE	= (float) 0.50;
	
	/**
	 * Full constructor.
	 */
	public AsteroidField(boolean isVisible, int lastObservation, String name, int carbonStock, int carbon, int slots, Set<IBuilding> buildings, Player owner)
	{
		super(isVisible, lastObservation, name, carbonStock, carbon, slots, buildings, owner);
	}
}
