package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IAntiProbeMissile;
import org.axan.sep.common.db.sqlite.orm.IVersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedAntiProbeMissile;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public interface IVersionedAntiProbeMissile extends IAntiProbeMissile, IVersionedUnit
{
	public String getTargetOwner();
	public String getTargetName();
	public Integer getTargetTurn();
}
