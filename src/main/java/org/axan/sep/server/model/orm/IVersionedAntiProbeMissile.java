package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IAntiProbeMissile;
import org.axan.sep.server.model.orm.IVersionedUnit;
import org.axan.sep.server.model.orm.base.IBaseVersionedAntiProbeMissile;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eUnitType;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedAntiProbeMissile extends IAntiProbeMissile, IVersionedUnit
{
	public String getTargetName();
	public Integer getTargetTurn();
	public String getTargetOwner();
}
