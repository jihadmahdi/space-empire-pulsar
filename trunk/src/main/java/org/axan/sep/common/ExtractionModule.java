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
public class ExtractionModule implements IBuilding, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int FIRST_CARBON_COST = 1000;
	
	// Only if visible
	private final int nbBuild;
	private final float carbonProductionPerTurn;
	
	// Only if owner
	private final int nextBuildCost;
	
	/**
	 * Full constructor. 
	 */
	public ExtractionModule(int nbBuild, float carbonProductionPerTurn, int nextBuildCost)
	{
		this.nbBuild = nbBuild;
		this.carbonProductionPerTurn = carbonProductionPerTurn;
		this.nextBuildCost = nextBuildCost;
	}
	
	public int getNbBuild()
	{
		return nbBuild;
	}

	public float getCarbonProductionPerTurn()
	{
		return carbonProductionPerTurn;
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
		return nbBuild+" Extraction modules build, extract "+carbonProductionPerTurn+"c per turn.\n"+((nextBuildCost<0)?"Can't build more":"Next build cost "+nextBuildCost)+".";
	}
}
