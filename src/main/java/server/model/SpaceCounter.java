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

import common.Player;
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
	
	static class SpaceRoadDeliverer extends Unit implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		private final String sourceName;
		private final String destinationName;

		public SpaceRoadDeliverer(String name, Player owner, RealLocation sourceLocation, String sourceName, String destinationName)
		{
			super(name, owner, sourceLocation);
			this.sourceName = sourceName;
			this.destinationName = destinationName;
		}
		
		@Override
		public common.SpaceRoadDeliverer getPlayerView(int date, String playerLogin, boolean isVisible)
		{
			return new common.SpaceRoadDeliverer(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwner(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin));
		}

		@Override
		public double getSpeed()
		{
			return 1;
		}

		@Override
		public boolean startMove(RealLocation currentLocation, GameBoard currentGameBoard)
		{
			return (getDestinationLocation() != null && super.startMove(currentLocation, currentGameBoard));		
		}
		
		@Override
		public void endMove(RealLocation currentLocation, GameBoard gameBoard)
		{
			setDestinationLocation(null);
			super.endMove(currentLocation, gameBoard);
			gameBoard.tryToLinkSpaceRoad(this);						
		}
		
		public void launch(RealLocation sourceLocation, RealLocation destinationLocation)
		{
			setSourceLocation(sourceLocation);
			setDestinationLocation(destinationLocation);
			setTravellingProgress(0);
		}
		
		public String getSourceName()
		{
			return sourceName;
		}
		
		public String getDestinationName()
		{
			return destinationName;
		}
		
	}
	
	static class CarbonCarrier extends Unit implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		/**
		 * @param name
		 * @param owner
		 */
		public CarbonCarrier(String name, Player owner, RealLocation sourceLocation)
		{
			super(name, owner, sourceLocation);			
		}

		/* (non-Javadoc)
		 * @see server.model.Unit#getPlayerView(int, java.lang.String, boolean)
		 */
		@Override
		public common.CarbonCarrier getPlayerView(int date, String playerLogin, boolean isVisible)
		{
			return new common.CarbonCarrier(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwner(), getSourceLocation(), getDestinationLocation(), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin));
		}

		@Override
		public double getSpeed()
		{
			return 1;
		}

		@Override
		public boolean startMove(RealLocation currentLocation, GameBoard currentGameBoard)
		{
			return (getDestinationLocation() != null && super.startMove(currentLocation, currentGameBoard));			
		}
		
		@Override
		public void endMove(RealLocation currentLocation, GameBoard gameBoard)
		{
			// NOP
			super.endMove(currentLocation, gameBoard);
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
	private final Set<SpaceRoad> spaceRoadsBuilt;
	private final Set<SpaceRoad> spaceRoadsLinked;
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
		this.spaceRoadsBuilt = new HashSet<SpaceRoad>();
		this.spaceRoadsLinked = new HashSet<SpaceRoad>();
		this.ordersToReceive = new HashSet<CarbonOrder>();
		this.currentSentOrder = new HashSet<CarbonOrder>();
		this.nextOrders = new Stack<CarbonOrder>();
		this.lastBuildDate = lastBuildDate;
	}
	
	private SpaceCounter(int lastBuildDate, int nbBuild, Set<SpaceRoad> spaceRoadsBuilt, Set<SpaceRoad> spaceRoadsLinked, Set<CarbonOrder> ordersToReceive, Set<CarbonOrder> currentSentOrder, Stack<CarbonOrder> nextOrders)
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
			if (r.getDestination().compareTo(destinationName) == 0)
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
			if (r.getSource().compareTo(sourceName) == 0)
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
			if (r.getDestination().compareTo(destinationName) == 0)
			{
				spaceRoadsBuilt.remove(r);
				break;
			}
		}
		
		for(SpaceRoad r : spaceRoadsLinked)
		{
			if (r.getSource().compareTo(destinationName) == 0)
			{
				spaceRoadsLinked.remove(r);
				break;
			}
		}
	}
}
