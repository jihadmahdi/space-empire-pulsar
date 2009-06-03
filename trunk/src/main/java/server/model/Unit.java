/**
 * @author Escallier Pierre
 * @file Unit.java
 * @date 3 juin 2009
 */
package server.model;

import common.Player;

/**
 * 
 */
abstract class Unit
{
	// Constants	
	private final String name;
	private final Player owner;
	
	/**
	 * Full constructor.
	 */
	public Unit(String name, Player owner)
	{
		this.name = name;
		this.owner = owner;
	}
}
