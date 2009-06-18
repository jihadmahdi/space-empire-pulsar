/**
 * @author Escallier Pierre
 * @file PulsarLauchingPad.java
 * @date 1 juin 2009
 */
package server.model;

import java.io.Serializable;

/**
 * 
 */
class PulsarLauchingPad implements IBuilding, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final int lastBuildDate;
	
	// Variables
	private int nbBuild;
	private int nbFired;
	
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
	
	private int getNextBuildCost()
	{
		// TODO : Redefine the formula
		return (int) (1+nbBuild * 0.25) * 1000;
	}
	
	/* (non-Javadoc)
	 * @see server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public common.PulsarLauchingPad getPlayerView(int date, String playerLogin)
	{
		return new common.PulsarLauchingPad(nbBuild, nbFired, getTotalBonus(), getNextBuildCost());
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
}
