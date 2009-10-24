/**
 * @author Escallier Pierre
 * @file GovernmentModule.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;

/**
 * Represent a government module build on a planet.
 */
public class GovernmentModule implements IBuilding, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	/* (non-Javadoc)
	 * @see common.IBuilding#getBuildSlotsCount()
	 */
	@Override
	public int getBuildSlotsCount()
	{
		return 1;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Government module gives a +50% bonus to population per turn and +50% bonus to carbon resource extraction on the current planet.";
	}
}
