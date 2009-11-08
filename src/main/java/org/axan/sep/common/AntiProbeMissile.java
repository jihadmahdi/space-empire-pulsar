/**
 																																								* @author Escallier Pierre
 * @file AntiProbeMissile.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;

import org.axan.sep.common.SEPUtils.RealLocation;


/**
 * Represent an anti-probe missile.
 */
public class AntiProbeMissile extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int PRICE_CARBON = 200;
	
	public static final int PRICE_POPULATION  = 0;
	
	private final boolean fired;
	
	/**
	 * Full constructor. 
	 */
	public AntiProbeMissile(boolean isVisible, int lastObservation, String name, String ownerName, RealLocation sourceLocation, RealLocation targetLocation, RealLocation currentLocation, double travellingProgress, double speed, boolean fired)
	{
		super(isVisible, lastObservation, name, ownerName, sourceLocation, targetLocation, currentLocation, travellingProgress, speed);
		this.fired = fired;																																																					
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append("\nStatus : "+(fired?"fired":"not fired"));
		return sb.toString();
	}
}
