package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IPulsarMissile;
import org.axan.sep.server.model.orm.IVersionedUnit;
import org.axan.sep.server.model.orm.base.IBaseVersionedPulsarMissile;

public interface IVersionedPulsarMissile extends IPulsarMissile, IVersionedUnit
{
	public Integer getDirection_z();
	public Integer getDirection_y();
	public Integer getDirection_x();
}
