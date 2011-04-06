/**
 * @author Escallier Pierre
 * @file SpaceCounter.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Represent all space counter build on a celestial body.
 */
public class SpaceCounter extends ABuilding implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int CARBON_COST = 1000;
	
	// Constants
	private final int lastBuildDate;
	
	// Only if visible
	private final int nbBuild;
	
	private final Set<SpaceRoad> spaceRoadsBuilt;
	private final Set<SpaceRoad> spaceRoadsLinked;
	private final Set<CarbonCarrier> ordersToReceive;
	private final Set<CarbonCarrier> currentSentOrder;
	private final Stack<CarbonOrder> nextOrders;				
	
	/**
	 * Full constructor.
	 */
	public SpaceCounter(int lastBuildDate, int nbBuild, Set<SpaceRoad> spaceRoadsBuilt, Set<SpaceRoad> spaceRoadsLinked, Set<CarbonCarrier> ordersToReceive, Set<CarbonCarrier> currentSentOrder, Stack<org.axan.sep.common.CarbonOrder> nextOrders)
	{
		this.nbBuild = nbBuild;
		this.spaceRoadsBuilt = spaceRoadsBuilt;
		this.spaceRoadsLinked = spaceRoadsLinked;
		this.ordersToReceive = ordersToReceive;
		this.currentSentOrder = currentSentOrder;
		this.nextOrders = nextOrders;
		this.lastBuildDate = lastBuildDate;
	}
	
	/**
	 * First build constructor.
	 */
	public SpaceCounter(int lastBuildDate)
	{
		this(lastBuildDate, 1, new HashSet<SpaceRoad>(), new HashSet<SpaceRoad>(), new HashSet<CarbonCarrier>(), new HashSet<CarbonCarrier>(), new Stack<CarbonOrder>());
	}
	
	/* (non-Javadoc)
	 * @see common.IBuilding#getBuildSlotsCount()
	 */
	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}
	
	@Override
	public int getLastBuildDate()
	{
		return lastBuildDate;
	}
	
	public int getCurrentCarbonFreight()
	{
		int carbonFreight = 0;
		for(CarbonCarrier carrier : currentSentOrder)
		{
			carbonFreight += carrier.getOrderAmount();
		}
		
		return carbonFreight;
	}
	
	public int getMaxCarbonFreight()
	{
		// TODO : Change formula
		return (nbBuild+1)*10000;
	}
	
	@Override
	public int getUpgradeCarbonCost()
	{
		return CARBON_COST;
	}
	
	@Override
	boolean canUpgrade()
	{
		return true;
	}
	
	@Override
	int getUpgradePopulationCost()
	{
		return 0;
	}
	
	@Override
	SpaceCounter getUpgraded(int date)
	{
		return new SpaceCounter(date, nbBuild+1, spaceRoadsBuilt, spaceRoadsLinked, ordersToReceive, currentSentOrder, nextOrders);
	}
	
	@Override
	SpaceCounter getDowngraded()
	{
		if (!canDowngrade()) throw new Error("Cannot currently downgrade this SpaceCounter.");		
		return new SpaceCounter(lastBuildDate, Math.max(0, nbBuild-1), spaceRoadsBuilt, spaceRoadsLinked, ordersToReceive, currentSentOrder, nextOrders);
	}

	@Override
	boolean canDowngrade()
	{
		return spaceRoadsBuilt.size() <= Math.max(0,nbBuild-1);
	}

	public int getAvailableRoadsBuilder()
	{
		return nbBuild - spaceRoadsBuilt.size();
	}
	
	public void buildSpaceRoad(SpaceRoad spaceRoad)
	{
		if (getAvailableRoadsBuilder() <= 0) throw new SEPCommonImplementationException("Cannot build space road");
		spaceRoadsBuilt.add(spaceRoad);
	}
	
	public void linkSpaceRoad(SpaceRoad spaceRoad)
	{
		spaceRoadsLinked.add(spaceRoad);
	}

	public SpaceRoad getSpaceRoad(String otherSideName)
	{
		for(SpaceRoad r : spaceRoadsBuilt)
		{
			if (r.getDestination().equals(otherSideName))
			{
				return r;
			}
		}
		
		for(SpaceRoad r : spaceRoadsLinked)
		{
			if (r.getSource().equals(otherSideName))
			{
				return r;
			}
		}
		
		return null;
	}
	
	public boolean hasSpaceRoadTo(String destinationName)
	{
		for(SpaceRoad r : spaceRoadsBuilt)
		{
			if (r.getDestination().equals(destinationName))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasSpaceRoadLinkedFrom(String sourceName)
	{
		for(SpaceRoad r : spaceRoadsLinked)
		{
			if (r.getSource().equals(sourceName))
			{
				return true;
			}
		}
		
		return false;
	}

	public void cutSpaceRoadLinkWith(String destinationName)
	{
		for(SpaceRoad r : spaceRoadsBuilt)
		{
			if (r.getDestination().equals(destinationName))
			{
				spaceRoadsBuilt.remove(r);
				break;
			}
		}
		
		for(SpaceRoad r : spaceRoadsLinked)
		{
			if (r.getSource().equals(destinationName))
			{
				spaceRoadsLinked.remove(r);
				break;
			}
		}
	}
	
	public void modifyCarbonOrder(Stack<CarbonOrder> nextCarbonOrders)
	{
		nextOrders.clear();
		nextOrders.addAll(nextCarbonOrders);	
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
		if (ordersToReceive.size() > 0)
		{
			sb.append("To receive :\n");
			for(CarbonCarrier o : ordersToReceive)
			{
				sb.append("  "+o+"\n");
			}
		}
		if (currentSentOrder.size() > 0)
		{
			sb.append("sent :\n");
			for(CarbonCarrier o : currentSentOrder)
			{
				sb.append("  "+o+"\n");
			}
		}
		if (nextOrders.size() > 0)
		{
			sb.append("next send :\n");
			for(CarbonOrder o : nextOrders)
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
		return nextOrders;
	}
	
	public Set<CarbonCarrier> getCurrentCarbonOrders()
	{
		return currentSentOrder;
	}
	
	public Set<CarbonCarrier> getCarbonOrdersToReceive()
	{
		return ordersToReceive;
	}
	
	public boolean hasSpaceRoadWith(String celestialBodyName)
	{
		if (spaceRoadsBuilt != null) for(SpaceRoad r : spaceRoadsBuilt)
		{
			if (celestialBodyName.equals(r.getSource()) || celestialBodyName.equals(r.getDestination())) return true;
		}
		
		if (spaceRoadsLinked != null) for(SpaceRoad r : spaceRoadsLinked)
		{
			if (celestialBodyName.equals(r.getSource()) || celestialBodyName.equals(r.getDestination())) return true;
		}
		
		return false;
	}
	
	 public void cutSpaceRoadLink(String celestialBodyName)
	{
		if (spaceRoadsBuilt != null) for(SpaceRoad r : spaceRoadsBuilt)
		{
			if (celestialBodyName.equals(r.getSource()) || celestialBodyName.equals(r.getDestination()))
			{
				spaceRoadsBuilt.remove(r);
				return;
			}
		}
		
		if (spaceRoadsLinked != null) for(SpaceRoad r : spaceRoadsLinked)
		{
			if (celestialBodyName.equals(r.getSource()) || celestialBodyName.equals(r.getDestination()))
			{
				spaceRoadsLinked.remove(r);
			}
		}
		
		throw new SEPCommonImplementationException("SpaceRoad link between with '"+celestialBodyName+"' does not exist.");
	}
}
