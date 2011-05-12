package org.axan.sep.common.db;

import org.axan.sep.common.db.IAntiProbeMissile;
import org.axan.sep.common.db.IVersionedUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public interface IVersionedAntiProbeMissile extends IAntiProbeMissile, IVersionedUnit
{
	public String getTargetOwner();
	public String getTargetName();
	public Integer getTargetTurn();
}
