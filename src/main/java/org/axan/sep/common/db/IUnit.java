package org.axan.sep.common.db;

import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public interface IUnit
{
	public String getOwner();
	public String getName();
	public eUnitType getType();
	public Location getDeparture();
	public double getProgress();
	public Location getDestination();
	public float getSight();
	public boolean isMoving();
}
