/**
 * @author Escallier Pierre
 * @file ICelestialBody.java
 * @date 3 juin 2009
 */
package server.model;

import common.Player;

/**
 * 
 */
interface ICelestialBody
{
	/** Get this celestial body owner (null if neutral). */
	Player getOwner();

	/**
	 * @param date
	 * @param playerLogin
	 * @param isVisible
	 * @return
	 */
	common.ICelestialBody getPlayerView(int date, String playerLogin, boolean isVisible);

	String getName();
}
