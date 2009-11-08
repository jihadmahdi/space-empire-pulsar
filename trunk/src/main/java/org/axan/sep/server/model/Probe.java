/**
 * @author Escallier Pierre
 * @file Probe.java
 * @date 4 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;

import org.axan.sep.common.SEPUtils.RealLocation;


/**
 * 
 */
public class Probe extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Variables
	private boolean deployed;
	
	// Views
	private final PlayerDatedView<Boolean> playersDeployedView = new PlayerDatedView<Boolean>();
	
	/**
	 * Full constructor. 
	 */
	public Probe(DataBase db, String name, String ownerName, RealLocation sourceLocation, boolean deployed)
	{
		super(db, name, ownerName, sourceLocation);
		this.deployed = deployed;
	}

	/**
	 * @return
	 */
	public boolean isDeployed()
	{
		return deployed;
	}

	/* (non-Javadoc)
	 * @see org.axan.sep.server.model.Unit#getPlayerView(int, java.lang.String)
	 */
	@Override
	public org.axan.sep.common.Probe getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			playersDeployedView.updateView(playerLogin, deployed, date);			
		}
		
		return new org.axan.sep.common.Probe(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwnerName(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin), getSpeedView(date, playerLogin, isVisible), playersDeployedView.getLastValue(playerLogin, false)); 
	}

	@Override
	public double getSpeed()
	{
		// TODO
		return 2;
	}

	@Override
	public boolean startMove()
	{
		return (getDestinationLocation() != null && super.startMove());		
	}
	
	@Override
	public void endMove()
	{
		deployed = true;
		super.endMove();
	}

	public void launch(RealLocation destination)
	{
		setSourceLocation(getRealLocation());
		setDestinationLocation(destination);
		setTravellingProgress(0);
	}
}
