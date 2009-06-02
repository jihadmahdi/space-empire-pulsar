/**
 * @author Escallier Pierre
 * @file GovernmentModule.java
 * @date 1 juin 2009
 */
package server.model;

import common.Player;

/**
 * This is a government module build on a celestial body.
 */
public class GovernmentModule extends Building
{
	private final Player owner;
	
	/**
	 * Full constructor 
	 */
	public GovernmentModule(Player owner)
	{
		this.owner = owner;
	}
}
