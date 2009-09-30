package server.model;

import java.io.Serializable;

import server.SEPServer;

import common.SEPUtils.RealLocation;

class CarbonCarrier extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final common.CarbonOrder order;
	
	/**
	 * @param name
	 * @param owner
	 */
	public CarbonCarrier(DataBase db, String name, String ownerName, RealLocation sourceLocation, common.CarbonOrder order)
	{
		super(db, name, ownerName, sourceLocation);
		this.order = order;
	}

	/* (non-Javadoc)
	 * @see server.model.Unit#getPlayerView(int, java.lang.String, boolean)
	 */
	@Override
	public common.CarbonCarrier getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		return new common.CarbonCarrier(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwnerName(), getSourceLocation(), getDestinationLocation(), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin), (isVisible?order:null));
	}

	@Override
	public double getSpeed()
	{
		return 1;
	}

	@Override
	public boolean startMove()
	{
		return (getDestinationLocation() != null && super.startMove());			
	}
	
	@Override
	public void endMove()
	{
		ProductiveCelestialBody destination = db.getCelestialBody(getRealLocation().asLocation(), ProductiveCelestialBody.class);
		if (destination == null) throw new SEPServer.SEPImplementationException("Cannot deliver carbon on location '"+getRealLocation()+"', no ProductiveCelestialBody found there.");	
		
		ProductiveCelestialBody source = db.getCelestialBody(order.getSourceName(), ProductiveCelestialBody.class);
		if (source == null) throw new SEPServer.SEPImplementationException("Cannot deliver carbon from '"+order.getSourceName()+"', unknown ProductiveCelestialBody.");

		destination.setCarbon(destination.getCarbon() + order.getAmount());
		
		// TODO : Travel log
		
		// The source celestial body may have been taken by another player, so we must test if the player is still the same.
		if (source.getOwnerName().equals(getOwnerName()))
		{
			SpaceCounter sourceSpaceCounter = source.getBuilding(SpaceCounter.class);
			if (sourceSpaceCounter != null)
			{
				sourceSpaceCounter.validateSentCarbonDelivery(this);
			}
		}
		
		SpaceCounter destinationSpaceCounter = destination.getBuilding(SpaceCounter.class);
		if (destinationSpaceCounter != null)
		{
			destinationSpaceCounter.validateReceivedCarbonDelivery(this);
		}
		
		db.removeUnit(getKey());
		
		super.endMove();
	}

	public void launch(RealLocation destination)
	{
		setSourceLocation(getRealLocation());
		setDestinationLocation(destination);
		setTravellingProgress(0);
	}
	
	public int getOrderAmount()
	{
		return order.getAmount();
	}
}
