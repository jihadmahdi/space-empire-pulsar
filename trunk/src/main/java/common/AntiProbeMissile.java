/**
 * @author Escallier Pierre
 * @file AntiProbeMissile.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * Represent an anti-probe missile.
 */
public class AntiProbeMissile extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final boolean fired;
	
	/**
	 * Full constructor. 
	 */
	public AntiProbeMissile(boolean isVisible, int lastObservation, String name, Player owner, int[] sourceLocation, int[] targetLocation, int[] currentEstimatedLocation, boolean fired)
	{
		super(isVisible, lastObservation, name, owner, sourceLocation, targetLocation, currentEstimatedLocation);
		this.fired = fired;
	}
}
