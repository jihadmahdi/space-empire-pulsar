/**
 * @author Escallier Pierre
 * @file SpaceCounter.java
 * @date 1 juin 2009
 */
package server.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import server.SEPServer;

import common.CarbonOrder;
import common.Player;
import common.Protocol.ServerRunningGame.RunningGameCommandException;
import common.SEPUtils.RealLocation;

/**
 * 
 */
class SpaceCounter extends ABuilding implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	static class SpaceRoad implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		// Constants
		private final int creationDate;
		private final String source;
		private final String destination;
		private final int speed;
		
		public SpaceRoad(int creationDate, String source, String destination, int speed)
		{
			this.creationDate = creationDate;
			this.source = source;
			this.destination = destination;
			this.speed = speed;
		}
		
		public common.SpaceRoad getPlayerView(int date, String playerLogin)
		{
			return new common.SpaceRoad(creationDate, true, source, destination, speed);
		}

		public String getDestination()
		{
			return destination;
		}
		
		public String getSource()
		{
			return source;
		}
	}
	
	static class CarbonDelivery implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		private CarbonCarrier carbonCarrier;
		private common.CarbonOrder order;
		
		/**
		 * 
		 */
		public CarbonDelivery()
		{
			// TODO Auto-generated constructor stub
		}
		
		public common.CarbonDelivery getPlayerView(int date, String playerLogin)
		{
			return new common.CarbonDelivery(carbonCarrier.getPlayerView(date, playerLogin, true), order.getAmount());
		}
		
		public int getAmount()
		{
			return order.getAmount();
		}
	}
	
	// Constants
	private final int lastBuildDate;
	
	// Variables
	private int nbBuild;
	// assert Sets.size() == nbBuild
	private final Set<SpaceRoad> spaceRoadsBuilt;
	private final Set<SpaceRoad> spaceRoadsLinked;
	private final Set<CarbonDelivery> ordersToReceive;
	private final Set<CarbonDelivery> currentSentOrder;
	private final Stack<common.CarbonOrder> nextOrders;
	
	
	/**
	 * First build constructor.
	 */
	public SpaceCounter(int lastBuildDate)
	{
		this(lastBuildDate, 1);
	}
	
	/**
	 * Full constructor.
	 */
	public SpaceCounter(int lastBuildDate, int nbBuild)
	{
		this.nbBuild = nbBuild;
		this.spaceRoadsBuilt = new HashSet<SpaceRoad>();
		this.spaceRoadsLinked = new HashSet<SpaceRoad>();
		this.ordersToReceive = new HashSet<CarbonDelivery>();
		this.currentSentOrder = new HashSet<CarbonDelivery>();
		this.nextOrders = new Stack<common.CarbonOrder>();
		this.lastBuildDate = lastBuildDate;
	}
	
	private SpaceCounter(int lastBuildDate, int nbBuild, Set<SpaceRoad> spaceRoadsBuilt, Set<SpaceRoad> spaceRoadsLinked, Set<CarbonDelivery> ordersToReceive, Set<CarbonDelivery> currentSentOrder, Stack<common.CarbonOrder> nextOrders)
	{
		this.nbBuild = nbBuild;
		this.spaceRoadsBuilt = spaceRoadsBuilt;
		this.spaceRoadsLinked = spaceRoadsLinked;
		this.ordersToReceive = ordersToReceive;
		this.currentSentOrder = currentSentOrder;
		this.nextOrders = nextOrders;
		this.lastBuildDate = lastBuildDate;
	}

	/* (non-Javadoc)
	 * @see server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public common.SpaceCounter getPlayerView(int date, String playerLogin)
	{
		Set<common.SpaceRoad> spaceRoadsBuiltSet = new HashSet<common.SpaceRoad>();
		for(SpaceRoad r : spaceRoadsBuilt)
		{
			spaceRoadsBuiltSet.add(r.getPlayerView(date, playerLogin));
		}
		
		Set<common.SpaceRoad> spaceRoadsLinkedSet = new HashSet<common.SpaceRoad>();
		for(SpaceRoad r : spaceRoadsLinked)
		{
			spaceRoadsLinkedSet.add(r.getPlayerView(date, playerLogin));
		}
		
		Set<common.CarbonDelivery> ordersToReceiveSet = new HashSet<common.CarbonDelivery>();
		for(CarbonDelivery o : ordersToReceive)
		{
			ordersToReceiveSet.add(o.getPlayerView(date, playerLogin));
		}
		
		Set<common.CarbonDelivery> currentSentOrderSet = new HashSet<common.CarbonDelivery>();
		for(CarbonDelivery o : currentSentOrder)
		{
			currentSentOrderSet.add(o.getPlayerView(date, playerLogin));
		}
		
		Stack<common.CarbonOrder> nextOrdersSet = new Stack<common.CarbonOrder>();
		for(common.CarbonOrder o : nextOrders)
		{
			nextOrdersSet.push(o);
		}
		
		return new common.SpaceCounter(nbBuild, spaceRoadsBuiltSet, spaceRoadsLinkedSet, ordersToReceiveSet, currentSentOrderSet, nextOrdersSet, getMaxCarbonFreight(), getCurrentCarbonFreight());
	}

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
		for(CarbonDelivery delivery : currentSentOrder)
		{
			carbonFreight += delivery.getAmount();
		}
		
		return carbonFreight;
	}
	
	public int getMaxCarbonFreight()
	{
		// TODO : Change formula
		return (nbBuild+1)*10000;
	}

	@Override
	int getUpgradeCarbonCost()
	{
		return common.SpaceCounter.CARBON_COST;
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
		if (getAvailableRoadsBuilder() <= 0) throw new SEPServer.SEPImplementationException("Cannot build space road");
		spaceRoadsBuilt.add(spaceRoad);
	}

	public void linkSpaceRoad(SpaceRoad spaceRoad)
	{
		spaceRoadsLinked.add(spaceRoad);
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
}
