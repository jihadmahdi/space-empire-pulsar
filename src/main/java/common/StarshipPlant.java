/**
 * @author Escallier Pierre
 * @file StarshipPlant.java
 * @date 3 juin 2009
 */
package common;

import java.util.Map;

/**
 * Represent a starship plant build on a celestial body.
 */
public class StarshipPlant implements IBuilding
{
	// Only if visible
	private final Map<Class<? extends IStarship>, Integer> landedStarships;
	
	/**
	 * Full constructor.
	 */
	public StarshipPlant(Map<Class<? extends IStarship>, Integer> landedStarships)
	{
		this.landedStarships = landedStarships;
	}
}
