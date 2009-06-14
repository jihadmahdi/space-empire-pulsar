/**
 * @author Escallier Pierre
 * @file DefenseModule.java
 * @date 1 juin 2009
 */
package server.model;

/**
 * 
 */
class DefenseModule implements IBuilding
{
	// Variables
	private int nbBuild;
	
	/**
	 * Full constructor. 
	 */
	public DefenseModule(int nbBuild)
	{
		this.nbBuild = nbBuild;
	}

	/* (non-Javadoc)
	 * @see server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public common.DefenseModule getPlayerView(int date, String playerLogin)
	{
		return new common.DefenseModule(nbBuild, getTotalBonus(), getNextBuildCost());
	}
	
	private float getTotalBonus()
	{
		// TODO : Redefine the formula
		return Float.valueOf(nbBuild)* (float) 0.25;
	}
	
	public int getNextBuildCost()
	{
		// TODO : Redefine the formula
		return (int) (1+nbBuild * 0.25) * 1000;
	}

	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}
}
