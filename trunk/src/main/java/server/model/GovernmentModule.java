/**
 * @author Escallier Pierre
 * @file GovernmentModule.java
 * @date 1 juin 2009
 */
package server.model;

import java.io.Serializable;

import server.SEPServer;

import common.Player;
import common.Protocol;

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
	 * @see server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public common.GovernmentModule getPlayerView(int date, String playerLogin)
	{
		return new common.GovernmentModule();
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
