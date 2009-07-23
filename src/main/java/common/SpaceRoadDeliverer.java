package common;

import java.io.Serializable;

import common.SEPUtils.RealLocation;

public class SpaceRoadDeliverer extends Unit implements Serializable
{	
	public SpaceRoadDeliverer(boolean isVisible, int lastObervation, String name, Player owner, RealLocation sourceLocation, RealLocation destinationLocation, RealLocation currentLocation, double travellingProgress)
	{
		super(isVisible, lastObervation, name, owner, sourceLocation, destinationLocation, currentLocation, travellingProgress);
	}
}
