/**
 * @author Escallier Pierre
 * @file Vortex.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;


/**
 * Represet a vortex.
 */
public class Vortex implements ICelestialBody, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final boolean isVisible;	
	private final int lastObservation;
	private final String name;

	/**
	 * Full constructor.
	 */
	public Vortex(boolean isVisible, int lastObservation, String name)
	{
		this.isVisible = isVisible;
		this.lastObservation = lastObservation;
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see common.Observable#getLastObservation()
	 */
	@Override
	public int getLastObservation()
	{
		return lastObservation;
	}

	/* (non-Javadoc)
	 * @see common.Observable#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return isVisible;
	}

	/* (non-Javadoc)
	 * @see common.ICelestialBody#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}
}
