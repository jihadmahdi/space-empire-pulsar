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
	
	public static final int PRICE_CARBON = 300;
	
	public static final int PRICE_POPULATION = 900;
	
	private final boolean deployed;
	
	/**
	 * Full constructor. 
	 */
	public Probe(boolean isVisible, int lastObservation, String name, Player owner, int[] sourceLocation, int[] destinationLocation, int[] currentEstimatedLocation, boolean deployed)
	{
		super(isVisible, lastObservation, name, owner, sourceLocation, destinationLocation, currentEstimatedLocation);
		this.deployed = deployed;
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append("\nStatus : "+(deployed?"deployed":"not deployed"));
		return sb.toString();
	}
}
