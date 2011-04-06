/**
 * @author Escallier Pierre
 * @file GovernmentModule.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;



/**
 * This is a government module build on a celestial body.
 */
class GovernmentModule extends ABuilding implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final int buildDate;
	
	public GovernmentModule(int buildDate)
	{
		this.buildDate = buildDate;
	}
	
	/* (non-Javadoc)
	 * @see org.axan.sep.server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public org.axan.sep.common.GovernmentModule getPlayerView(int date, String playerLogin)
	{
		return new org.axan.sep.common.GovernmentModule(buildDate);
	}

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
	int getUpgradeCarbonCost()
	{
		return 0;
	}

	@Override
	int getUpgradePopulationCost()
	{
		return 0;
	}

	@Override
	GovernmentModule getUpgraded(int date)
	{
		throw new Error("Cannot upgrade GovernmentModule");
	}
	
	@Override
	boolean canUpgrade()
	{
		return false;
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
	
}
