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
}
