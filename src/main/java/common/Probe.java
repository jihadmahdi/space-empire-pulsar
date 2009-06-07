/**
 * @author Escallier Pierre
 * @file Probe.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;

/**
 * Represent a probe.
 */
public class Probe extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int PROBE_SCORE = 3;
	
	private final boolean deployed;
	
	/**
	 * Full constructor. 
	 */
	public Probe(boolean isVisible, int lastObservation, String name, Player owner, int[] sourceLocation, int[] destinationLocation, int[] currentEstimatedLocation, boolean deployed)
	{
		super(isVisible, lastObservation, name, owner, sourceLocation, destinationLocation, currentEstimatedLocation);
		this.deployed = deployed;
	}
}
