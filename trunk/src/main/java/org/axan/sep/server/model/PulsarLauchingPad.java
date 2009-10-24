/**
 * @author Escallier Pierre
 * @file PulsarLauchingPad.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;


/**
 * 
 */
class PulsarLauchingPad extends ABuilding implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final int lastBuildDate;
	
	// Variables
	private int nbBuild;
	private int nbFired;
	
	/**
	 * First build constructor.
	 */
	public PulsarLauchingPad(int lastBuildDate)
	{
		this(lastBuildDate, 1, 0);
	}
	
	/**
	 * Full constructor. 
	 */
	public PulsarLauchingPad(int lastBuildDate, int nbBuild, int nbFired)
	{
		this.nbBuild = nbBuild;
		this.nbFired = nbFired;
		this.lastBuildDate = lastBuildDate;
	}

	private float getTotalBonus()
	{
		// TODO : Redefine the formula
		return Float.valueOf(nbBuild-nbFired)* (float) 0.25;
	}
	
	/* (non-Javadoc)
	 * @see server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public org.axan.sep.common.PulsarLauchingPad getPlayerView(int date, String playerLogin)
	{
		return new org.axan.sep.common.PulsarLauchingPad(nbBuild, nbFired, getTotalBonus(), getUpgradeCarbonCost());
	}

	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}
	
	public int getUnusedCount()
	{
		return nbBuild - nbFired;
	}

	public PulsarLauchingPad getUpgradedBuilding()
	{
		return new PulsarLauchingPad(lastBuildDate, nbBuild+1, nbFired);
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
		return (int) (1+nbBuild * 0.25) * 1000;
	}

	@Override
	int getUpgradePopulationCost()
	{
		// TODO : Redefine the formula
		return (int) (1+nbBuild * 0.25) * 1000;
	}

	@Override
	PulsarLauchingPad getUpgraded(int date)
	{
		return new PulsarLauchingPad(date, nbBuild+1, nbFired);
	}
	
	@Override
	PulsarLauchingPad getDowngraded()
	{
		throw new Error("Cannot downgrade PulsarLaunchingPad");
	}

	@Override
	boolean canDowngrade()
	{
		return false;
	}
}
