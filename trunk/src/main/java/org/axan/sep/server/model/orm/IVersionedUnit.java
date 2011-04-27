package org.axan.sep.server.model.orm;

import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.model.orm.IUnit;
import org.axan.sep.server.model.orm.base.IBaseVersionedUnit;

public interface IVersionedUnit extends IUnit
{
	public RealLocation getDeparture();
	public RealLocation getDestination();
	public Double getProgress();
	public Integer getTurn();
}
