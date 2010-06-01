/**
 * @author Escallier Pierre
 * @file StarshipPlant.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;


/**
 * Represent a starship plant build on a celestial body.
 */
public class StarshipPlant extends ABuilding implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int POPULATION_COST = 500;
	public static final int CARBON_COST = 2000;
		
	// Constants
	private final int lastBuildDate;
	
	/**
	 * Full constructor.
	 */
	public StarshipPlant(int lastBuildDate)
	{
		this.lastBuildDate = lastBuildDate;
	}
	
	/* (non-Javadoc)
	 * @see common.IBuilding#getBuildSlotsCount()
	 */
	@Override
	public int getBuildSlotsCount()
	{
		return 1;
	}
	
	@Override
	public int getLastBuildDate()
	{
		return lastBuildDate;
	}
	
	@Override
	public int getUpgradeCarbonCost()
	{
		return 0;
	}

	@Override
	int getUpgradePopulationCost()
	{
		return 0;
	}
	
	@Override
	boolean canUpgrade()
	{
		return false;
	}

	@Override
	ABuilding getUpgraded(int date)
	{
		return null;
	}
	
	@Override
	SpaceCounter getDowngraded()
	{
		return null;
	}

	@Override
	boolean canDowngrade()
	{
		return true;
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
