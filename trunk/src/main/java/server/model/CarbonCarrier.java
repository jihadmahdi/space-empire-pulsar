package server.model;

import java.io.Serializable;

import common.SEPUtils.RealLocation;

class CarbonCarrier extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	/**
	 * @param name
	 * @param owner
	 */
	public CarbonCarrier(DataBase db, String name, String ownerName, RealLocation sourceLocation)
	{
		super(db, name, ownerName, sourceLocation);			
	}

	/* (non-Javadoc)
	 * @see server.model.Unit#getPlayerView(int, java.lang.String, boolean)
	 */
	@Override
	public common.CarbonCarrier getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		return new common.CarbonCarrier(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwnerName(), getSourceLocation(), getDestinationLocation(), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin));
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
		// NOP
		super.endMove();
	}
}
