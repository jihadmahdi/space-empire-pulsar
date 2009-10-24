/**
 * @author Escallier Pierre
 * @file Pulsar.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;

import org.axan.sep.common.SEPUtils.RealLocation;


/**
 * Represent a pulsar estimated effect marker.
 * This marker is only for owner (and softcore allied).
 */
public class EstimatedPulsarMarker implements IMarker, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Only if owner (estimation)
	private final int creationDate;
	private final boolean isVisible;
	
	private final RealLocation targetLocation;
	private int estimatedVolume;
	private int estimatedTime;
	
	/**
	 * Full constructor.
	 */
	public EstimatedPulsarMarker(int creationDate, boolean isVisible, RealLocation targetLocation, int estimatedVolume, int estimatedTime)
	{
		this.creationDate = creationDate;
		this.isVisible = isVisible;
		this.targetLocation = targetLocation;
		this.estimatedVolume = estimatedVolume;
		this.estimatedTime = estimatedTime;
	}

	/* (non-Javadoc)
	 * @see common.IMarker#getCreationDate()
	 */
	@Override
	public int getCreationDate()
	{
		return creationDate;
	}

	/* (non-Javadoc)
	 * @see common.IMarker#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return isVisible;
	}
}
