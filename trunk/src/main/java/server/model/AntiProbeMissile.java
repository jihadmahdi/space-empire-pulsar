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
	public AntiProbeMissile(GameBoard gameBoard, String name, String ownerName, RealLocation sourceLocation, boolean fired)
	{
		super(gameBoard, name, ownerName, sourceLocation);
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
		
		return new common.AntiProbeMissile(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwnerName(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin), playersFiredView.getLastValue(playerLogin, false)); 
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
		setDestinationLocation(null);
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