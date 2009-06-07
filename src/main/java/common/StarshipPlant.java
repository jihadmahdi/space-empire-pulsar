/**
 * @author Escallier Pierre
 * @file StarshipPlant.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.Map;

/**
 * Represent a starship plant build on a celestial body.
 */
public class StarshipPlant implements IBuilding, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int PRICE_POPULATION = 500;
	public static final int PRICE_CARBON = 2000;
	
	// Only if visible
	private final Map<Class<? extends IStarship>, Integer> landedStarships;
	
	/**
	 * Full constructor.
	 */
	public StarshipPlant(Map<Class<? extends IStarship>, Integer> landedStarships)
	{
		this.landedStarships = landedStarships;
	}
	
	/* (non-Javadoc)
	 * @see common.IBuilding#getBuildSlotsCount()
	 */
	@Override
	public int getBuildSlotsCount()
	{
		return 1;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Starship plant";
	}
}