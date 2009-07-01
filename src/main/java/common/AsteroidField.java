/**
 * @author Escallier Pierre
 * @file FilteredAsteroidField.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class AsteroidField extends ProductiveCelestialBody implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int		CARBON_MIN		= 60 * 1000;

	public static final int		CARBON_MAX		= 300 * 1000;

	public static final int		SLOTS_MIN		= 3;

	public static final int		SLOTS_MAX		= 6;

	public static final float	GENERATION_RATE	= (float) 0.50;
	
	/**
	 * Full constructor.
	 */
	public AsteroidField(boolean isVisible, int lastObservation, String name, int carbonStock, int carbon, int slots, Set<IBuilding> buildings, Player owner, Map<Class<? extends IStarship>, Integer> unasignedFleet)
	{
		super(isVisible, lastObservation, name, carbonStock, carbon, slots, buildings, owner, unasignedFleet);
	}

	/* (non-Javadoc)
	 * @see common.ProductiveCelestialBody#canBuildType(java.lang.Class)
	 */
	@Override
	public boolean canBuildType(Class<? extends IBuilding> buildingType)
	{
		if (ExtractionModule.class.equals(buildingType)) return true;
		if (DefenseModule.class.equals(buildingType)) return true;
		if (SpaceCounter.class.equals(buildingType)) return true;
		return false;
	}
}
