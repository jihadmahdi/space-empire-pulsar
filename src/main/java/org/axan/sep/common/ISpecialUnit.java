package org.axan.sep.common;

/**
 * Represent a special unit that can join a fleet.
 */
public interface ISpecialUnit
{
	/**
	 * Return the special unit name. Should be unique for each players.
	 * @return
	 */
	String getName();

	/**
	 * Should always return the same value for a same instance, if false the getPlayerView will never be called (special unit is not visible to players).
	 * @return
	 */
	boolean isVisibleToClients();		

	/**
	 * Should always return the same value for a same instance, if true the special unit can join fleets.
	 * @return
	 */
	boolean canJoinFleet();
}
