/**
 * @author Escallier Pierre
 * @file FilteredNebula.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class Nebula extends ProductiveCelestialBody implements Serializable
{	
	private static final long	serialVersionUID	= 1L;
	
	public static final int CARBON_MIN = 100*1000;
	
	public static final int CARBON_MAX = 500*1000;
	
	public static final int SLOTS_MIN = 2;
	
	public static final int SLOTS_MAX = 4;
	
	public static final float GENERATION_RATE = (float) 0.20;
	
	/**
	 * Full constructor.
	 */
	public Nebula(boolean isVisible, int lastObservation, String name, int carbonStock, int carbon, int slots, Set<IBuilding> buildings, Player owner, Map<Class<? extends IStarship>, Integer> unasignedFleet)
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
		if (SpaceCounter.class.equals(buildingType)) return true;
		return false;
	}
}
