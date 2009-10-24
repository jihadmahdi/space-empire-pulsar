/**
 * @author Escallier Pierre
 * @file DefenseModule.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;

/**
 * 
 */
class DefenseModule extends ABuilding implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final int lastBuildDate;
	
	// Variables
	private int nbBuild;	
	
	/**
	 * First build constructor
	 */
	public DefenseModule(int lastBuildDate)
	{
		this(lastBuildDate, 1); 
	}
	
	/**
	 * Full constructor. 
	 */
	public DefenseModule(int lastBuildDate, int nbBuild)
	{
		this.nbBuild = nbBuild;
		this.lastBuildDate = lastBuildDate;
	}

	/* (non-Javadoc)
	 * @see server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public org.axan.sep.common.DefenseModule getPlayerView(int date, String playerLogin)
	{
		return new org.axan.sep.common.DefenseModule(nbBuild, getTotalBonus(), getUpgradeCarbonCost());
	}
	
	private float getTotalBonus()
	{
		// TODO : Redefine the formula
		return Float.valueOf(nbBuild)* (float) 0.25;
	}		

	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}
	
	public DefenseModule getUpgradedBuilding(int lastBuildDate)
	{
		return new DefenseModule(lastBuildDate, nbBuild+1);
	}

	@Override
	public int getLastBuildDate()
	{
		return lastBuildDate;
	}

	@Override
	int getUpgradeCarbonCost()
	{
		return (int) ((Float.valueOf(1+nbBuild) * 0.25) * 1000);
	}

	@Override
	int getUpgradePopulationCost()
	{
		return 0;
	}

	@Override
	DefenseModule getUpgraded(int date)
	{
		return new DefenseModule(date, nbBuild+1);
	}

	@Override
	DefenseModule getDowngraded()
	{
		return new DefenseModule(lastBuildDate, Math.max(0, nbBuild-1));
	}

	@Override
	boolean canDowngrade()
	{
		return nbBuild > 0;
	}
}
