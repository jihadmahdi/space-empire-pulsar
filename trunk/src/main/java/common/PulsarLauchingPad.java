/**
 * @author Escallier Pierre
 * @file PulsarLauchingPad.java
 * @date 3 juin 2009
 */
package common;

/**
 * Represent all pulsar lauching pad build on a ceslestial body (including already fired ones).
 */
public class PulsarLauchingPad implements IBuilding
{
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
}
