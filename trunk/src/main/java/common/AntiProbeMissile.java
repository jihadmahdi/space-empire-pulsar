/**
 * @author Escallier Pierre
 * @file AntiProbeMissile.java
 * @date 3 juin 2009
 */
package common;

/**
 * Represent an anti-probe missile.
 */
public class AntiProbeMissile extends Unit
{
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
