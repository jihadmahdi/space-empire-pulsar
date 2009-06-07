/**
 * @author Escallier Pierre
 * @file ExtractionModule.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * Represent all extraction modules build on a celestial body.
 */
public class ExtractionModule implements IBuilding, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int FIRST_BUILD_COST = 1000;
	
	// Only if visible
	private final int nbBuild;
	private final float totalBonus;
	
	// Only if owner
	private final int nextBuildCost;
	
	/**
	 * Full constructor. 
	 */
	public ExtractionModule(int nbBuild, float totalBonus, int nextBuildCost)
	{
		this.nbBuild = nbBuild;
		this.totalBonus = totalBonus;
		this.nextBuildCost = nextBuildCost;
	}
	
	public int getNbBuild()
	{
		return nbBuild;
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
		return nbBuild+" Extraction modules build, give an extraction bonus of "+totalBonus+".\n"+((nextBuildCost<0)?"Can't build more":"Next build cost "+nextBuildCost)+".";
	}
}
