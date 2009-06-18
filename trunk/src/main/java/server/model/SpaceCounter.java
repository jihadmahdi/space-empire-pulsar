/**
 * @author Escallier Pierre
 * @file SpaceCounter.java
 * @date 1 juin 2009
 */
package server.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import common.Player;

/**
 * 
 */
class SpaceCounter implements IBuilding, Serializable
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
	private final Set<CarbonOrder> nextOrders;
	
	/**
	 * Full constructor.
	 */
	public SpaceCounter(int lastBuildDate, int nbBuild)
	{
		this.nbBuild = nbBuild;
		this.spaceRoads = new HashSet<SpaceRoad>();
		this.ordersToReceive = new HashSet<CarbonOrder>();
		this.currentSentOrder = new HashSet<CarbonOrder>();
		this.nextOrders = new HashSet<CarbonOrder>();
		this.lastBuildDate = lastBuildDate;
	}
	
	private SpaceCounter(int lastBuildDate, int nbBuild, Set<SpaceRoad> spaceRoads, Set<CarbonOrder> ordersToReceive, Set<CarbonOrder> currentSentOrder, Set<CarbonOrder> nextOrders)
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
		
		Set<common.CarbonOrder> nextOrdersSet = new HashSet<common.CarbonOrder>();
		for(CarbonOrder o : nextOrders)
		{
			nextOrdersSet.add(o.getPlayerView(date, playerLogin));
		}
		
		return new common.SpaceCounter(nbBuild, spaceRoadsSet, ordersToReceiveSet, currentSentOrderSet, nextOrdersSet);
	}

	@Override
	public int getBuildSlotsCount()
	{
		return nbBuild;
	}

	public SpaceCounter getUpgradedBuilding(int lastBuildDate)
	{
		return new SpaceCounter(lastBuildDate, nbBuild+1, spaceRoads, ordersToReceive, currentSentOrder, nextOrders);
	}

	@Override
	public int getLastBuildDate()
	{
		return lastBuildDate;
	}
}
