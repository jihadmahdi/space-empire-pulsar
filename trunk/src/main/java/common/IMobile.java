/**
 * @author Escallier Pierre
 * @file Mobile.java
 * @date 3 juin 2009
 */
package common;

import common.SEPUtils.RealLocation;

/**
 * 
 */
public interface IMobile
{
	RealLocation getSourceLocation();
	RealLocation getDestinationLocation();
	double getTravellingProgress();
	RealLocation getCurrentLocation();
}
