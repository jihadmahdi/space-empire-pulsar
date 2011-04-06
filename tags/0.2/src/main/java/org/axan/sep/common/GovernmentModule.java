/**
 * @author Escallier Pierre
 * @file GovernmentModule.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;

/**
 * Represent a government module build on a planet.
 */
public class GovernmentModule extends ABuilding implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final int buildDate;
	
	public GovernmentModule(int buildDate)
	{
		this.buildDate = buildDate;
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
		return buildDate;
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
	GovernmentModule getUpgraded(int date)
	{
		throw new Error("Cannot upgrade GovernmentModule");
	}

	@Override
	GovernmentModule getDowngraded()
	{
		throw new Error("Cannot downgrade GovernmentModule");
	}

	@Override
	boolean canDowngrade()
	{
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Government module gives a +50% bonus to population per turn and +50% bonus to carbon resource extraction on the current planet.";
	}
}
