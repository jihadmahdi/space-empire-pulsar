/**
 * @author Escallier Pierre
 * @file DefeneModule.java
 * @date 3 juin 2009
 */
package common;

/**
 * Represent all defense modules build on a celestial body.
 */
public class DefeneModule implements IBuilding
{
	// Only if visible
	private final int nbBuild;
	private final float totalBonus;
	
	// Only if owner
	private final int nextBuildCost;
	
	/**
	 * Full constructor. 
	 */
	public DefeneModule(int nbBuild, float totalBonus, int nextBuildCost)
	{
		this.nbBuild = nbBuild;
		this.totalBonus = totalBonus;
		this.nextBuildCost = nextBuildCost;
	}
}
