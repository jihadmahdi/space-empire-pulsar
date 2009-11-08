package org.axan.sep.common;

import java.io.Serializable;

import org.axan.sep.common.SEPUtils.RealLocation;


public class SpaceRoadDeliverer extends Unit implements Serializable
{	
	public SpaceRoadDeliverer(boolean isVisible, int lastObervation, String name, String ownerName, RealLocation sourceLocation, RealLocation destinationLocation, RealLocation currentLocation, double travellingProgress, double speed)
	{
		super(isVisible, lastObervation, name, ownerName, sourceLocation, destinationLocation, currentLocation, travellingProgress, speed);
	}
}
