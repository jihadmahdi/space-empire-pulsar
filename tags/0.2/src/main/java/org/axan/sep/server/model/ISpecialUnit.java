package org.axan.sep.server.model;


interface ISpecialUnit
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
	 * Return the client view for this special unit.
	 * @param date
	 * @param playerLogin
	 * @param isVisible
	 * @return
	 */
	org.axan.sep.common.ISpecialUnit getPlayerView(int date, String playerLogin, boolean isVisible);

	/**
	 * Should always return the same value for a same instance, if true the special unit can join fleets.
	 * @return
	 */
	boolean canJoinFleet();
	
}
