/**
 * @author Escallier Pierre
 * @file StarshipPlant.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;




/**
 * 
 */
class StarshipPlant extends ABuilding implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final int lastBuildDate;
	
	/**
	 * First build constructor.
	 */
	public StarshipPlant(int lastBuildDate)
	{
		this.lastBuildDate = lastBuildDate;
	}
	
	/* (non-Javadoc)
	 * @see org.axan.sep.server.model.IBuilding#getPlayerView(int, java.lang.String)
	 */
	@Override
	public org.axan.sep.common.StarshipPlant getPlayerView(int date, String playerLogin)
	{
		return new org.axan.sep.common.StarshipPlant(lastBuildDate);
	}

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
	ABuilding getUpgraded(int date)
	{
		return null;
	}
	
	@Override
	boolean canUpgrade()
	{
		return false;
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
	
}
