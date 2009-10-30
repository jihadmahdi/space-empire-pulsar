/**
 * @author Escallier Pierre
 * @file SpaceCounter.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.axan.sep.common.CarbonOrder;
import org.axan.sep.server.SEPServer;



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
		
		public org.axan.sep.common.SpaceRoad getPlayerView(int date, String playerLogin)
		{
			return new org.axan.sep.common.SpaceRoad(creationDate, true, source, destination, speed);
		}

		public String getDestination()
		{
			return destination;
		}
		
		public String getSource()
		{
			return source;
		}
		
		public double getSpeed()
		{
			return speed;
		}
	}
	
	// Constants
	private final int lastBuildDate;
	
	// Variables
	private int nbBuild;
	// assert Sets.size() == nbBuild
	private final Set<SpaceRoad> spaceRoadsBuilt;
	private final Set<SpaceRoad> spaceRoadsLinked;
	private final Set<CarbonCarrier> ordersToReceive;
	private final Set<CarbonCarrier> currentSentOrder;
	private final Stack<org.axan.sep.common.CarbonOrder> nextOrders;
	
	
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
		this.ordersToReceive = new HashSet<CarbonCarrier>();
		this.currentSentOrder = new HashSet<CarbonCarrier>();
		this.nextOrders = new Stack<org.axan.sep.common.CarbonOrder>();
		this.lastBuildDate = lastBuildDate;
	}
	
	private SpaceCounter(int lastBuildDate, int nbBuild, Set<SpaceRoad> spaceRoadsBuilt, Set<SpaceRoad> spaceRoadsLinked, Set<CarbonCarrier> ordersToReceive, Set<CarbonCarrier> currentSentOrder, Stack<org.axan.sep.common.CarbonOrder> nextOrders)
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
	 * @see org.axan.sep.server.model.IBuilding#getPlayerView(java.lang.String)
	 */
	@Override
	public org.axan.sep.common.SpaceCounter getPlayerView(int date, String playerLogin)
	{
		Set<org.axan.sep.common.SpaceRoad> spaceRoadsBuiltSet = new HashSet<org.axan.sep.common.SpaceRoad>();
		for(SpaceRoad r : spaceRoadsBuilt)
		{
			spaceRoadsBuiltSet.add(r.getPlayerView(date, playerLogin));
		}
		
		Set<org.axan.sep.common.SpaceRoad> spaceRoadsLinkedSet = new HashSet<org.axan.sep.common.SpaceRoad>();
		for(SpaceRoad r : spaceRoadsLinked)
		{
			spaceRoadsLinkedSet.add(r.getPlayerView(date, playerLogin));
		}
		
		Set<org.axan.sep.common.CarbonCarrier> ordersToReceiveSet = new HashSet<org.axan.sep.common.CarbonCarrier>();
		for(CarbonCarrier o : ordersToReceive)
		{
			ordersToReceiveSet.add(o.getPlayerView(date, playerLogin, true));
		}
		
		Set<org.axan.sep.common.CarbonCarrier> currentSentOrderSet = new HashSet<org.axan.sep.common.CarbonCarrier>();
		for(CarbonCarrier o : currentSentOrder)
		{
			currentSentOrderSet.add(o.getPlayerView(date, playerLogin, true));
		}
		
		Stack<org.axan.sep.common.CarbonOrder> nextOrdersSet = new Stack<org.axan.sep.common.CarbonOrder>();
		for(org.axan.sep.common.CarbonOrder o : nextOrders)
		{
			nextOrdersSet.push(o);
		}
		
		return new org.axan.sep.common.SpaceCounter(nbBuild, spaceRoadsBuiltSet, spaceRoadsLinkedSet, ordersToReceiveSet, currentSentOrderSet, nextOrdersSet, getMaxCarbonFreight(), getCurrentCarbonFreight());
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
	int getUpgradeCarbonCost()
	{
		return org.axan.sep.common.SpaceCounter.CARBON_COST;
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

	public void prepareCarbonDelivery(DataBase db, ProductiveCelestialBody source)
	{
		int delivererId = 0;
		
		while(!nextOrders.isEmpty() && nextOrders.firstElement().getAmount() + getCurrentCarbonFreight() <= getMaxCarbonFreight() && nextOrders.firstElement().getAmount() <= source.getCarbon())
		{
			CarbonOrder order = nextOrders.firstElement();
			
			if (!source.getName().equals(order.getSourceName())) throw new Error("SpaceCounter / Order source inconsistency : "+source.getName()+" != "+order.getSourceName());
			
			ProductiveCelestialBody destination = db.getCelestialBody(order.getDestinationName(), ProductiveCelestialBody.class);
			if (destination == null) throw new SEPServer.SEPImplementationException("Order destination error : "+order.getDestinationName());
			
			SpaceCounter destinationSpaceCounter = destination.getBuilding(SpaceCounter.class);
			if (destinationSpaceCounter == null) throw new SEPServer.SEPImplementationException("Cannot found destination space counter on "+destination.getName());
			
			CarbonCarrier carrier = new CarbonCarrier(db, String.format("CarbonDeliverer-%d%x", db.getDate(), delivererId), source.getOwnerName(), source.getLocation().asRealLocation(), order);
			carrier.launch(destination.getLocation().asRealLocation());			
			db.insertUnit(carrier);
			
			currentSentOrder.add(carrier);
			destinationSpaceCounter.ordersToReceive.add(carrier);
			
			source.setCarbon(source.getCarbon() - order.getAmount());
			
			++delivererId;
			
			if (order.isAutomated())
			{
				nextOrders.add(order);
			}
			
			nextOrders.remove(0);
		}
	}
	
	public void validateSentCarbonDelivery(CarbonCarrier carrier)
	{
		currentSentOrder.remove(carrier);
	}
	
	public void validateReceivedCarbonDelivery(CarbonCarrier carrier)
	{
		ordersToReceive.remove(carrier);
	}
}
