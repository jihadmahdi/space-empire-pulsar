/**
 * @author Escallier Pierre
 * @file Probe.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;

import org.axan.sep.common.SEPUtils.RealLocation;


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
	
	private transient boolean launched = false;
	
	/**
	 * Full constructor. 
	 */
	public Probe(boolean isVisible, int lastObservation, String name, String ownerName, RealLocation sourceLocation, RealLocation destinationLocation, RealLocation currentLocation, double travellingProgress, double speed, boolean deployed)
	{
		super(isVisible, lastObservation, name, ownerName, sourceLocation, destinationLocation, currentLocation, travellingProgress, speed);
		this.deployed = deployed;
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append("\nStatus : "+(deployed?"deployed":"not deployed"));
		return sb.toString();
	}

	public boolean isDeployed()
	{
		return deployed;
	}
	
	public boolean isLaunched()
	{
		return launched;
	}
	
	void launch(RealLocation destination)
	{
		launched = true;
		setDestinationLocation(destination);
	}
}
