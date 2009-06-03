/**
 * @author Escallier Pierre
 * @file Mobile.java
 * @date 3 juin 2009
 */
package common;

/**
 * 
 */
public interface IMobile
{
	int[] getSourceLocation();
	int[] getDestinationLocation();
	int[] getCurrentEstimatedLocation();
}
