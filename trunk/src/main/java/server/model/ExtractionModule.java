/**
 * @author Escallier Pierre
 * @file ExctractionModule.java
 * @date 1 juin 2009
 */
package server.model;

import java.io.Serializable;

/**
 * 
 */
class ExtractionModule implements IBuilding, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Variables
	private int nbBuild;
	
	/**
	 * Full constructor.
	 */
	public ExtractionModule(int nbBuild)
	{
		this.nbBuild = nbBuild;
	}
	
	/* (non-Javadoc)
	 * @see server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public common.ExtractionModule getPlayerView(int date, String playerLogin)
	{
		return new common.ExtractionModule(nbBuild, getTotalBonus(), getNextBuildCost());
	}
	
	private float getTotalBonus()
	{
		// TODO : Redefine the formula
		return Float.valueOf(nbBuild)* (float) 0.25;
	}
	
	int getNextBuildCost()
	{
		// TODO : Redefine the formula
		return (int) ((Float.valueOf(nbBuild+1) * 0.25) * 1000);
	}

	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}
	
	public ExtractionModule getUpgradedBuilding()
	{
		return new ExtractionModule(nbBuild+1);
	}
}
