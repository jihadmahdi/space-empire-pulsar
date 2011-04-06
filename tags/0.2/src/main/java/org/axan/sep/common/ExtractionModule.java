/**
 * @author Escallier Pierre
 * @file ExtractionModule.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;


/**
 * Represent all extraction modules build on a celestial body.
 */
public class ExtractionModule extends ABuilding implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int FIRST_CARBON_COST = 1000;
	
	// Constants
	private final int lastBuildDate;
	
	// Only if visible
	private final int nbBuild;
	
	/**
	 * Full constructor. 
	 */
	public ExtractionModule(int lastBuildDate, int nbBuild)
	{
		this.nbBuild = nbBuild;
		this.lastBuildDate = lastBuildDate;
	}
	
	/**
	 * First build constructor.
	 */
	public ExtractionModule(int lastBuildDate)
	{
		this(lastBuildDate, 1);
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
	
	@Override
	public int getLastBuildDate()
	{
		return lastBuildDate;
	}
	
	@Override
	public int getUpgradeCarbonCost()
	{
		// TODO : Redefine the formula
		return (int) ((Float.valueOf(nbBuild+1) * 0.25) * 1000);
	}
	
	@Override
	boolean canUpgrade()
	{
		return true;
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
	ExtractionModule getDowngraded()
	{
		return new ExtractionModule(lastBuildDate, Math.max(0, nbBuild-1));
	}
	
	@Override
	boolean canDowngrade()
	{
		return nbBuild > 0;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return nbBuild+" Extraction modules build, extract "+getCarbonProductionPerTurn()+"C per turn.\n"+((getUpgradeCarbonCost()<0 && getUpgradePopulationCost()<0)?"Can't build more":"Next build cost "+getUpgradeCarbonCost())+"c, "+getUpgradePopulationCost()+"P.";
	}
}
