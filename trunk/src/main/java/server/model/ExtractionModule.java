/**
 * @author Escallier Pierre
 * @file ExctractionModule.java
 * @date 1 juin 2009
 */
package server.model;

/**
 * 
 */
class ExtractionModule implements IBuilding
{
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
	
	private int getNextBuildCost()
	{
		// TODO : Redefine the formula
		return (int) (nbBuild * 0.25) * 1000;
	}
}
