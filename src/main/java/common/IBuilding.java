/**
 * @author Escallier Pierre
 * @file Building.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represent a building on a productive celestial body, from a specific player point of view.
 */
public interface IBuilding
{
	/**
	 * @return
	 */
	int getBuildSlotsCount();

}
