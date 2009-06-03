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
}
