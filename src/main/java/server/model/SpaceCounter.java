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

import common.Player;

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
		private final int[] from;
		private final int[] to;
		private final int speed;
		
		public SpaceRoad(int creationDate, int[] from, int[] to, int speed)
		{
			this.creationDate = creationDate;
			this.from = from;
			this.to = to;
			this.speed = speed;
		}
		
		public common.SpaceRoad getPlayerView(int date, String playerLogin)
		{
			return new common.SpaceRoad(creationDate, true, from, to, speed);
		}
	}
	
	static class CarbonCarrier extends Unit implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		/**
		 * @param name
		 * @param owner
		 */
		public CarbonCarrier(String name, Player owner)
		{
			super(name, owner);			
		}

		/* (non-Javadoc)
		 * @see server.model.Unit#getPlayerView(int, java.lang.String, boolean)
		 */
		@Override
		public common.CarbonCarrier getPlayerView(int date, String playerLogin, boolean isVisible)
		{
			return new common.CarbonCarrier(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwner(), getSourceLocation(), getDestinationLocation(), getCurrentEstimatedLocation());
		}
		
	}
	
	static class CarbonOrder implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		private CarbonCarrier carbonCarrier;
		private int carbonAmount;
		
		/**
		 * 
		 */
		public CarbonOrder()
		{
			// TODO Auto-generated constructor stub
		}
		
		public common.CarbonOrder getPlayerView(int date, String playerLogin)
		{
			return new common.CarbonOrder(carbonCarrier.getPlayerView(date, playerLogin, true), carbonAmount);
		}
	}
	
	// Constants
	private final int lastBuildDate;
	
	// Variables
	private int nbBuild;
	// assert Sets.size() == nbBuild
	private final Set<SpaceRoad> spaceRoads;
	private final Set<CarbonOrder> ordersToReceive;
	private final Set<CarbonOrder> currentSentOrder;
	private final Stack<CarbonOrder> nextOrders;
	
	
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
		this.spaceRoads = new HashSet<SpaceRoad>();
		this.ordersToReceive = new HashSet<CarbonOrder>();
		this.currentSentOrder = new HashSet<CarbonOrder>();
		this.nextOrders = new Stack<CarbonOrder>();
		this.lastBuildDate = lastBuildDate;
	}
	
	private SpaceCounter(int lastBuildDate, int nbBuild, Set<SpaceRoad> spaceRoads, Set<CarbonOrder> ordersToReceive, Set<CarbonOrder> currentSentOrder, Stack<CarbonOrder> nextOrders)
	{
		this.nbBuild = nbBuild;
		this.spaceRoads = spaceRoads;
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
		Set<common.SpaceRoad> spaceRoadsSet = new HashSet<common.SpaceRoad>();
		for(SpaceRoad r : spaceRoads)
		{
			spaceRoadsSet.add(r.getPlayerView(date, playerLogin));
		}
		
		Set<common.CarbonOrder> ordersToReceiveSet = new HashSet<common.CarbonOrder>();
		for(CarbonOrder o : ordersToReceive)
		{
			ordersToReceiveSet.add(o.getPlayerView(date, playerLogin));
		}
		
		Set<common.CarbonOrder> currentSentOrderSet = new HashSet<common.CarbonOrder>();
		for(CarbonOrder o : currentSentOrder)
		{
			currentSentOrderSet.add(o.getPlayerView(date, playerLogin));
		}
		
		Stack<common.CarbonOrder> nextOrdersSet = new Stack<common.CarbonOrder>();
		for(CarbonOrder o : nextOrders)
		{
			nextOrdersSet.push(o.getPlayerView(date, playerLogin));
		}
		
		return new common.SpaceCounter(nbBuild, spaceRoadsSet, ordersToReceiveSet, currentSentOrderSet, nextOrdersSet, getMaxCarbonFreight(), getCurrentCarbonFreight());
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
		for(CarbonOrder order : currentSentOrder)
		{
			carbonFreight += order.carbonAmount;
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
		return new SpaceCounter(date, nbBuild+1, spaceRoads, ordersToReceive, currentSentOrder, nextOrders);
	}
	
	@Override
	SpaceCounter getDowngraded()
	{
		if (!canDowngrade()) throw new Error("Cannot currently downgrade this SpaceCounter.");		
		return new SpaceCounter(lastBuildDate, Math.max(0, nbBuild-1), spaceRoads, ordersToReceive, currentSentOrder, nextOrders);
	}

	@Override
	boolean canDowngrade()
	{
		return spaceRoads.size() <= Math.max(0,nbBuild-1);
	}
}
