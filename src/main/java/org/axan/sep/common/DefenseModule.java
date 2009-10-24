/**
 * @author Escallier Pierre
 * @file DefeneModule.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;


/**
 * Represent all defense modules build on a celestial body.
 */
public class DefenseModule implements IBuilding, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int FIRST_CARBON_COST = 4000;
	
	// Only if visible
	private final int nbBuild;
	private final float totalBonus;
	
	// Only if owner
	private final int nextBuildCost;
	
	/**
	 * Full constructor. 
	 */
	public DefenseModule(int nbBuild, float totalBonus, int nextBuildCost)
	{
		this.nbBuild = nbBuild;
		this.totalBonus = totalBonus;
		this.nextBuildCost = nextBuildCost;
	}

	public float getTotalBonus()
	{
		return totalBonus;
	}

	public int getNextBuildCost()
	{
		return nextBuildCost;
	}

	/* (non-Javadoc)
	 * @see common.IBuilding#getBuildSlotsCount()
	 */
	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return nbBuild+" Defense modules build, give a defense bonus of "+totalBonus+".\n"+((nextBuildCost<0)?"Can't build more":"Next build cost "+nextBuildCost)+".";
	}
}
