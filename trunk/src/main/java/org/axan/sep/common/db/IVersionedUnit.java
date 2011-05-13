package org.axan.sep.common.db;

import org.axan.sep.common.SEPUtils.Location;

public interface IVersionedUnit extends IUnit
{
	public Integer getTurn();
	public Location getDeparture();
	public Double getProgress();
	public Location getDestination();
}
