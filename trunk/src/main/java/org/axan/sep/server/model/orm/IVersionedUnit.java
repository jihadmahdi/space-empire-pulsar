package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IUnit;

public interface IVersionedUnit extends IUnit
{
	public Integer getDeparture_z();
	public Integer getDeparture_y();
	public Integer getDeparture_x();
	public Integer getDestination_z();
	public Integer getDestination_y();
	public Double getProgress();
	public Integer getDestination_x();
	public Integer getTurn();
}
