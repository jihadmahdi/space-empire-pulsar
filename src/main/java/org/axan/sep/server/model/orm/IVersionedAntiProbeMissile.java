package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IAntiProbeMissile;
import org.axan.sep.server.model.orm.IVersionedUnit;
import org.axan.sep.server.model.orm.base.IBaseVersionedAntiProbeMissile;

public interface IVersionedAntiProbeMissile extends IAntiProbeMissile, IVersionedUnit
{
	public String getTargetName();
	public Integer getTargetTurn();
	public String getTargetOwner();
}
