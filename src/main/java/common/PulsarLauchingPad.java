/**
 * @author Escallier Pierre
 * @file PulsarLauchingPad.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * Represent all pulsar lauching pad build on a ceslestial body (including already fired ones).
 */
public class PulsarLauchingPad implements IBuilding, Serializable
{
	public static final int POPULATION_COST = 50000;
	public static final int CARBON_COST = 100000;
	
	private static final long	serialVersionUID	= 1L;
	
	// Only if visible
	private final int nbBuild;
	private final int nbFired;
	private final float totalBonus;
	
	// Only if owner
	private final int nextBuildCost;
	
	/**
	 * Full constructor. 
	 */
	public PulsarLauchingPad(int nbBuild, int nbFired, float totalBonus, int nextBuildCost)
	{
		this.nbBuild = nbBuild;
		this.nbFired = nbFired;
		this.totalBonus = totalBonus;
		this.nextBuildCost = nextBuildCost;
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return (nbBuild-nbFired)+" pulsar launching pads ready to fire with a power bonus of "+totalBonus+", "+nbFired+" already used.\n"+((nextBuildCost<0)?"Can't build more":"Next build cost "+nextBuildCost);
	}		
}
