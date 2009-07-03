/**
 * @author Escallier Pierre
 * @file Probe.java
 * @date 4 juin 2009
 */
package server.model;

import java.io.Serializable;

import common.Player;
import common.SEPUtils.Location;

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
	public Probe(String name, Player owner, boolean deployed)
	{
		super(name, owner);
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
	 * @see server.model.Unit#getPlayerView(int, java.lang.String)
	 */
	@Override
	public common.Probe getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			playersDeployedView.updateView(playerLogin, deployed, date);			
		}
		
		return new common.Probe(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwner(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentEstimatedLocationView(playerLogin), playersDeployedView.getLastValue(playerLogin, false)); 
	}

	@Override
	public double getSpeed()
	{
		// TODO
		return 2;
	}

	@Override
	public boolean startMove(Location currentLocation, GameBoard currentGameBoard)
	{
		if (getDestinationLocation() != null && !isMoving())
		{
			setCurrentLocation(currentLocation);
			setSourceLocation(currentLocation);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void endMove(Location currentLocation, GameBoard gameBoard)
	{
		// NOP
	}
}
