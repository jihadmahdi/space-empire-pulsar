package server.model;

import java.io.Serializable;

import server.SEPServer;

import common.Player;
import common.SEPUtils.Location;

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
	public AntiProbeMissile(String name, Player owner, boolean fired)
	{
		super(name, owner);
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
	 * @see server.model.Unit#getPlayerView(int, java.lang.String)
	 */
	@Override
	public common.AntiProbeMissile getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			playersFiredView.updateView(playerLogin, fired, date);			
		}
		
		return new common.AntiProbeMissile(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwner(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentEstimatedLocationView(playerLogin), playersFiredView.getLastValue(playerLogin, false)); 
	}

	@Override
	public double getSpeed()
	{
		// TODO
		return 3;
	}

	@Override
	public boolean startMove(Location currentLocation, GameBoard currentGameBoard)
	{
		if (fired && !isMoving())
		{	
			setSourceLocation(currentLocation);
			setCurrentLocation(currentLocation);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void endMove(Location currentLocation, GameBoard gameBoard)
	{
		setDestinationLocation(null);
	}

	public void fire(String targetProbeOwnerName, String targetProbeName, Location source, Location destination)
	{
		if (fired) throw new SEPServer.SEPImplementationException("Error: AntiProbeMissile '"+getName()+"' already fired.");
		
		fired = true;
		
		targetName = targetProbeName;
		targetOwnerName = targetProbeOwnerName;
		
		setCurrentLocation(source);
		setSourceLocation(source);
		setDestinationLocation(destination);
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