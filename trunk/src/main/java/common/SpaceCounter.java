/**
 * @author Escallier Pierre
 * @file SpaceCounter.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.Set;

/**
 * Represent all space counter build on a celestial body.
 */
public class SpaceCounter implements IBuilding, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int PRICE = 1000;
	
	// Only if visible
	private final int nbBuild;
	
	private final Set<SpaceRoad> spaceRoads;
	
	private final Set<CarbonOrder> carbonToReceive;
	
	private final Set<CarbonOrder> nextCarbonOrder;
	
	private final Set<CarbonOrder> currentCarbonOrder;
	
	/**
	 * Full constructor.
	 */
	public SpaceCounter(int nbBuild, Set<SpaceRoad> spaceRoads, Set<CarbonOrder> carbonToReceive, Set<CarbonOrder> currentCarbonOrder, Set<CarbonOrder> nextCarbonOrder)
	{
		this.nbBuild = nbBuild;
		this.spaceRoads = spaceRoads;
		this.carbonToReceive = carbonToReceive;
		this.currentCarbonOrder = currentCarbonOrder;
		this.nextCarbonOrder = nextCarbonOrder;
	}
	
	/* (non-Javadoc)
	 * @see common.IBuilding#getBuildSlotsCount()
	 */
	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(nbBuild+" space roads build.\n");
		if (spaceRoads.size() > 0)
		{
			sb.append("Space Roads :\n");
			for(SpaceRoad r : spaceRoads)
			{
				sb.append("  "+r+"\n");
			}
		}
		if (carbonToReceive.size() > 0)
		{
			sb.append("To receive :\n");
			for(CarbonOrder o : carbonToReceive)
			{
				sb.append("  "+o+"\n");
			}
		}
		if (currentCarbonOrder.size() > 0)
		{
			sb.append("sent :\n");
			for(CarbonOrder o : currentCarbonOrder)
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
}
