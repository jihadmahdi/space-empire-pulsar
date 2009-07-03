/**
 * @author Escallier Pierre
 * @file Mobile.java
 * @date 3 juin 2009
 */
package common;

import common.SEPUtils.Location;

/**
 * 
 */
public interface IMobile
{
	Location getSourceLocation();
	Location getDestinationLocation();
	Location getCurrentEstimatedLocation();
}
