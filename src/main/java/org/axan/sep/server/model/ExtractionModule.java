/**
 * @author Escallier Pierre
 * @file ExctractionModule.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;

/**
 * 
 */
class ExtractionModule extends ABuilding implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final int lastBuildDate;
	
	// Variables
	private int nbBuild;
	
	/**
	 * First build constructor.
	 */
	public ExtractionModule(int lastBuildDate)
	{
		this(lastBuildDate, 1);
	}
	
	/**
	 * Full constructor.
	 */
	public ExtractionModule(int lastBuildDate, int nbBuild)
	{
		this.nbBuild = nbBuild;
		this.lastBuildDate = lastBuildDate;
	}
	
	/* (non-Javadoc)
	 * @see org.axan.sep.server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public org.axan.sep.common.ExtractionModule getPlayerView(int date, String playerLogin)
	{
		return new org.axan.sep.common.ExtractionModule(lastBuildDate, nbBuild);
	}
	
	public int getCarbonProductionPerTurn()
	{
		// TODO : Redefine the formula
		return (int) (5000 * (Float.valueOf(nbBuild) * (float) 0.25));
	}

	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}
	
	public ExtractionModule getUpgradedBuilding(int lastBuildDate)
	{
		return new ExtractionModule(lastBuildDate, nbBuild+1);
	}

	@Override
	public int getLastBuildDate()
	{
		return lastBuildDate;
	}

	@Override
	int getUpgradeCarbonCost()
	{
		// TODO : Redefine the formula
		return (int) ((Float.valueOf(nbBuild+1) * 0.25) * 1000);
	}

	@Override
	int getUpgradePopulationCost()
	{
		return 0;
	}

	@Override
	ExtractionModule getUpgraded(int date)
	{
		return new ExtractionModule(date, nbBuild+1);
	}
	
	@Override
	boolean canUpgrade()
	{
		return true;
	}
	
	@Override
	ExtractionModule getDowngraded()
	{
		return new ExtractionModule(lastBuildDate, Math.max(0, nbBuild-1));
	}

	@Override
	boolean canDowngrade()
	{
		return nbBuild > 0;
	}
}
