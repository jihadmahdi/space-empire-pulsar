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
	
	public static final int POPULATION_COST = 500;
	public static final int CARBON_COST = 2000;
		
	/**
	 * Full constructor.
	 */
	public StarshipPlant()
	{
		
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
