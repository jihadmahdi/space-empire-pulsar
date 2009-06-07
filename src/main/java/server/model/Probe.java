/**
 * @author Escallier Pierre
 * @file Probe.java
 * @date 4 juin 2009
 */
package server.model;

import common.Player;

/**
 * 
 */
public class Probe extends Unit
{
	// Constans
	private final int scope;
	
	// Variables
	private boolean deployed;
	
	// Views
	private final PlayerDatedView<Boolean> playersDeployedView = new PlayerDatedView<Boolean>();
	
	/**
	 * Full constructor. 
	 */
	public Probe(String name, Player owner, boolean deployed, int scope)
	{
		super(name, owner);
		this.deployed = deployed;
		this.scope = scope;
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
}
