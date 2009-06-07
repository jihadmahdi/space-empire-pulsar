/**
 * @author Escallier Pierre
 * @file PulsarLauchingPad.java
 * @date 1 juin 2009
 */
package server.model;

/**
 * 
 */
class PulsarLauchingPad implements IBuilding
{
	// Variables
	private int nbBuild;
	private int nbFired;
	
	/**
	 * Full constructor. 
	 */
	public PulsarLauchingPad(int nbBuild, int nbFired)
	{
		this.nbBuild = nbBuild;
		this.nbFired = nbFired;
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
}
