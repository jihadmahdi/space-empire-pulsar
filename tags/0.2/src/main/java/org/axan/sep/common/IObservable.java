/**
 * @author Escallier Pierre
 * @file Observable.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

/**
 * Represent an object that can be observed.
 * It keep track of last observation date and visibility state.
 */
public interface IObservable
{
	/** Is the current area visible for the player. */
	boolean isVisible();
	
	/** Last turn date this area has been visible. */
	int getLastObservation(); 
}
