package org.axan.sep.server.model;

import java.io.Serializable;
import java.util.Set;

import org.axan.sep.common.ALogEntry;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.SEPServer;



public class AntiProbeMissile extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Variables
	private boolean fired;
	private String targetName;
	private String targetOwnerName;
	
	// Views
	private final PlayerDatedView<Boolean> playersFiredView = new PlayerDatedView<Boolean>();	
	
	/**
	 * Full constructor. 
	 */
	public AntiProbeMissile(ISEPServerDataBase db, String name, String ownerName, RealLocation sourceLocation, boolean fired, Set<ALogEntry> travellingLogs)
	{
		super(db, name, ownerName, sourceLocation, travellingLogs);
		this.fired = fired;
	}

	/**
	 * @return
	 */
	public boolean isFired()
	{
		return fired;
	}

	/* (non-Javadoc)
	 * @see org.axan.sep.server.model.Unit#getPlayerView(int, java.lang.String)
	 */
	@Override
	public org.axan.sep.common.AntiProbeMissile getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			playersFiredView.updateView(playerLogin, fired, date);			
		}
		
		return new org.axan.sep.common.AntiProbeMissile(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwnerName(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin), getSpeedView(date, playerLogin, isVisible), playersFiredView.getLastValue(playerLogin, false)); 
	}	

	@Override
	public double getSpeed()
	{
		// TODO
		return 3;
	}

	@Override
	public boolean startMove()
	{
		return (fired && super.startMove());		
	}
	
	@Override
	public void endMove()
	{
		super.endMove();
	}

	public void fire(String targetProbeOwnerName, String targetProbeName, RealLocation source, RealLocation destination)
	{
		if (fired) throw new SEPServer.SEPImplementationException("Error: AntiProbeMissile '"+getName()+"' already fired.");
		
		fired = true;
		
		targetName = targetProbeName;
		targetOwnerName = targetProbeOwnerName;
		
		setSourceLocation(source);
		setDestinationLocation(destination);
		setTravellingProgress(0);		
	}
	
	public String getTargetName()
	{
		return targetName;
	}
	
	public String getTargetOwnerName()
	{
		return targetOwnerName;
	}
}