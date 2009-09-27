package server.model;

import java.io.Serializable;

import server.model.SpaceCounter.SpaceRoad;

import common.Protocol.ServerRunningGame.RunningGameCommandException;
import common.SEPUtils.RealLocation;

class SpaceRoadDeliverer extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final String sourceName;
	private final String destinationName;

	public SpaceRoadDeliverer(DataBase db, String name, String ownerName, RealLocation sourceLocation, String sourceName, String destinationName)
	{
		super(db, name, ownerName, sourceLocation);
		this.sourceName = sourceName;
		this.destinationName = destinationName;
	}
	
	@Override
	public common.SpaceRoadDeliverer getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		return new common.SpaceRoadDeliverer(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwnerName(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin));
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
		tryToLinkSpaceRoad();
		super.endMove();
	}
	
	private static class LinkSpaceRoadCheckResult
	{
		final SpaceCounter sourceSpaceCounter;
		final SpaceCounter destinationSpaceCounter;
		final SpaceRoad	spaceRoad;
		
		public LinkSpaceRoadCheckResult(SpaceCounter sourceSpaceCounter, SpaceCounter destinationSpaceCounter, SpaceRoad spaceRoad)
		{
			this.sourceSpaceCounter = sourceSpaceCounter;
			this.destinationSpaceCounter = destinationSpaceCounter;
			this.spaceRoad = spaceRoad;
		}
	}
	
	private LinkSpaceRoadCheckResult checkLinkSpaceRoad() throws RunningGameCommandException
	{
		if (sourceName.equals(destinationName)) throw new RunningGameCommandException("Cannot build space road locally.");
		
		ProductiveCelestialBody source = db.getCelestialBody(sourceName, ProductiveCelestialBody.class, getOwnerName());
		if (source == null) throw new RunningGameCommandException("Celestial body '"+sourceName+"' is not a productive celestial body.");
		
		SpaceCounter sourceSpaceCounter = source.getBuilding(SpaceCounter.class);
		if (sourceSpaceCounter == null) throw new RunningGameCommandException("'"+sourceName+"' has no space counter built.");
				
		
		ProductiveCelestialBody destination = db.getCelestialBody(destinationName, ProductiveCelestialBody.class);
		if (destination == null) throw new RunningGameCommandException("Celestial body '"+sourceName+"' is not a productive celestial body.");
		
		SpaceCounter destinationSpaceCounter = destination.getBuilding(SpaceCounter.class);
		if (destinationSpaceCounter == null) throw new RunningGameCommandException("'"+destinationName+"' has no space counter built.");		
	
		if (sourceSpaceCounter.hasSpaceRoadTo(destinationName))
		{
			throw new RunningGameCommandException("'"+sourceName+"' already has a space road to '"+destinationName+"'");
		}
		
		if (sourceSpaceCounter.hasSpaceRoadLinkedFrom(sourceName))
		{
			throw new RunningGameCommandException("'"+sourceName+"' already has a space road linked from '"+sourceName+"'");
		}

		SpaceRoad spaceRoad = new SpaceRoad(db.getDate(), sourceName, destinationName, db.getGameConfig().getSpaceRoadsSpeed());
		
		return new LinkSpaceRoadCheckResult(sourceSpaceCounter, destinationSpaceCounter, spaceRoad);
	}
	
	private boolean tryToLinkSpaceRoad()
	{
		LinkSpaceRoadCheckResult linkSpaceRoadCheckResult = null;
		try
		{
			linkSpaceRoadCheckResult = checkLinkSpaceRoad();						
		}
		catch(Throwable t)
		{
			linkSpaceRoadCheckResult = null;
		}
		
		db.removeUnit(getKey());
				
		if (linkSpaceRoadCheckResult == null)
		{
			// TODO: Log space road link impossibility
			// spaceRoadDeliverer.addTravelligLogEntry(logEntry)
			return false;
		}
		else
		{
			linkSpaceRoadCheckResult.sourceSpaceCounter.buildSpaceRoad(linkSpaceRoadCheckResult.spaceRoad);
			linkSpaceRoadCheckResult.destinationSpaceCounter.linkSpaceRoad(linkSpaceRoadCheckResult.spaceRoad);
			return true;
		}
	}
	
	public void launch(RealLocation sourceLocation, RealLocation destinationLocation)
	{
		setSourceLocation(sourceLocation);
		setDestinationLocation(destinationLocation);
		setTravellingProgress(0);
	}
	
	public String getSourceName()
	{
		return sourceName;
	}
	
	public String getDestinationName()
	{
		return destinationName;
	}
	
}
