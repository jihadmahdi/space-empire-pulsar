/**
 * @author Escallier Pierre
 * @file SpaceCounter.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.Stack;


/**
 * Represent all space counter build on a celestial body.
 */
public class SpaceCounter implements IBuilding, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int CARBON_COST = 1000;
	
	// Only if visible
	private final int nbBuild;
	
	private final Set<SpaceRoad> spaceRoadsBuilt;
	
	private final Set<SpaceRoad> spaceRoadsLinked;
	
	private final Set<CarbonCarrier> carbonToReceive;
	
	private final Stack<CarbonOrder> nextCarbonOrder;
	
	private final Set<CarbonCarrier> currentCarbonOrder;
	
	private final int maxCarbonFreight;
	private final int currentCarbonFreight;
	
	/**
	 * Full constructor.
	 */
	public SpaceCounter(int nbBuild, Set<SpaceRoad> spaceRoadsBuilt, Set<SpaceRoad> spaceRoadsLinked, Set<CarbonCarrier> carbonToReceive, Set<CarbonCarrier> currentCarbonOrder, Stack<CarbonOrder> nextCarbonOrder, int maxCarbonFreight, int currentCarbonFreight)
	{
		this.nbBuild = nbBuild;
		this.spaceRoadsBuilt = spaceRoadsBuilt;
		this.spaceRoadsLinked = spaceRoadsLinked;
		this.carbonToReceive = carbonToReceive;
		this.currentCarbonOrder = currentCarbonOrder;
		this.nextCarbonOrder = nextCarbonOrder;
		this.currentCarbonFreight= currentCarbonFreight;
		this.maxCarbonFreight = maxCarbonFreight;
	}
	
	/* (non-Javadoc)
	 * @see common.IBuilding#getBuildSlotsCount()
	 */
	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}
	
	public int getCurrentCarbonFreight()
	{
		return currentCarbonFreight;
	}
	
	public int getMaxCarbonFreight()
	{
		return maxCarbonFreight;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(nbBuild+" space counter(s) build.\n");
		if (spaceRoadsBuilt.size() > 0)
		{
			sb.append("Space Roads built :\n");
			for(SpaceRoad r : spaceRoadsBuilt)
			{
				sb.append("  "+r+"\n");
			}
		}
		if (spaceRoadsLinked.size() > 0)
		{
			sb.append("Space Roads linked :\n");
			for(SpaceRoad r : spaceRoadsLinked)
			{
				sb.append("  "+r+"\n");
			}
		}
		if (carbonToReceive.size() > 0)
		{
			sb.append("To receive :\n");
			for(CarbonCarrier o : carbonToReceive)
			{
				sb.append("  "+o+"\n");
			}
		}
		if (currentCarbonOrder.size() > 0)
		{
			sb.append("sent :\n");
			for(CarbonCarrier o : currentCarbonOrder)
			{
				sb.append("  "+o+"\n");
			}
		}
		if (nextCarbonOrder.size() > 0)
		{
			sb.append("next send :\n");
			for(CarbonOrder o : nextCarbonOrder)
			{
				sb.append("  "+o+"\n");
			}
		}
		
		return sb.toString();
	}

	public Set<SpaceRoad> getSpaceRoadsBuilt()
	{
		return spaceRoadsBuilt;
	}
	
	public Set<SpaceRoad> getSpaceRoadsLinked()
	{
		return spaceRoadsLinked;
	}
	
	public Stack<CarbonOrder> getNextCarbonOrders()
	{
		return nextCarbonOrder;
	}
	
	public Set<CarbonCarrier> getCurrentCarbonOrders()
	{
		return currentCarbonOrder;
	}
	
	public Set<CarbonCarrier> getCarbonOrdersToReceive()
	{
		return carbonToReceive;
	}
}
