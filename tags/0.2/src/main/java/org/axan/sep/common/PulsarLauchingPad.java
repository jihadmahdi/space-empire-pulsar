/**
 * @author Escallier Pierre
 * @file PulsarLauchingPad.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;

/**
 * Represent all pulsar lauching pad build on a ceslestial body (including already fired ones).
 */
public class PulsarLauchingPad extends ABuilding implements Serializable
{
	public static final int POPULATION_COST = 50000;
	public static final int CARBON_COST = 100000;
	
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final int lastBuildDate;
	
	// Only if visible
	private final int nbBuild;
	private final int nbFired;
	
	/**
	 * Full constructor. 
	 */
	public PulsarLauchingPad(int lastBuildDate, int nbBuild, int nbFired)
	{
		this.nbBuild = nbBuild;
		this.nbFired = nbFired;
		this.lastBuildDate = lastBuildDate;
	}
	
	/**
	 * First build constructor.
	 */
	public PulsarLauchingPad(int lastBuildDate)
	{
		this(lastBuildDate, 1, 0);
	}
	
	private float getTotalBonus()
	{
		// TODO : Redefine the formula
		return Float.valueOf(nbBuild-nbFired)* (float) 0.25;
	}

	/* (non-Javadoc)
	 * @see common.IBuilding#getBuildSlotsCount()
	 */
	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}
	
	public int getUnusedCount()
	{
		return nbBuild - nbFired;
	}
	
	@Override
	public int getLastBuildDate()
	{
		return lastBuildDate;
	}
	
	@Override
	boolean canUpgrade()
	{
		return true;
	}
	
	@Override
	public int getUpgradeCarbonCost()
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return (nbBuild-nbFired)+" pulsar launching pads ready to fire with a power bonus of "+getTotalBonus()+", "+nbFired+" already used.\n"+((getUpgradeCarbonCost()<0 && getUpgradePopulationCost()<0)?"Can't build more":"Next build cost "+getUpgradeCarbonCost()+"C, "+getUpgradePopulationCost()+"P.");
	}		
}
