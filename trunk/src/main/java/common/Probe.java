/**
 * @author Escallier Pierre
 * @file Probe.java
 * @date 3 juin 2009
 */
package common;

/**
 * Represent a probe.
 */
public class Probe extends Unit
{
	private final int scope;
	private final boolean deployed;
	
	/**
	 * Full constructor. 
	 */
	public Probe(boolean isVisible, int lastObservation, String name, Player owner, int[] sourceLocation, int[] destinationLocation, int[] currentEstimatedLocation, boolean deployed, int scope)
	{
		super(isVisible, lastObservation, name, owner, sourceLocation, destinationLocation, currentEstimatedLocation);
		this.deployed = deployed;
		this.scope = scope;
	}
}
