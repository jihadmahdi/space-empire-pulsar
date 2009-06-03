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
}
