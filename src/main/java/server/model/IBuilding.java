/**
 * @author Escallier Pierre
 * @file Building.java
 * @date 1 juin 2009
 */
package server.model;

/**
 * This abstract class represent a building on a celestial body.
 */
interface IBuilding
{

	/**
	 * @param playerLogin
	 * @return
	 */
	common.IBuilding getPlayerView(int date, String playerLogin);

}
