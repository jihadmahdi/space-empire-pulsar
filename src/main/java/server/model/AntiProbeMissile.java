package server.model;

import java.io.Serializable;

import server.SEPServer;

import common.Player;
import common.SEPUtils.RealLocation;

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
	public AntiProbeMissile(String name, Player owner, RealLocation sourceLocation, boolean fired)
	{
		super(name, owner, sourceLocation);
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
		
		return new common.AntiProbeMissile(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwner(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin), playersFiredView.getLastValue(playerLogin, false)); 
	}	

	@Override
	public double getSpeed()
	{
		// TODO
		return 3;
	}

	@Override
	public boolean startMove(RealLocation currentLocation, GameBoard currentGameBoard)
	{
		return (fired && super.startMove(currentLocation, currentGameBoard));		
	}
	
	@Override
	public void endMove(RealLocation currentLocation, GameBoard gameBoard)
	{
		setDestinationLocation(null);
		super.endMove(currentLocation, gameBoard);
	}

	public void fire(String targetProbeOwnerName, String targetProbeName, RealLocation source, RealLocation destination)
	{
		if (fired) throw new SEPServer.SEPImplementationException("Error: AntiProbeMissile '"+getName()+"' already fired.");
		
		fired = true;
		
		targetName = targetProbeName;
		targetOwnerName = targetProbeOwnerName;
		
		setTravellingProgress(0);
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