package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBaseVersionedUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public interface IVersionedUnit extends IUnit
{
	public Integer getTurn();
	public Location getDeparture();
	public Double getProgress();
	public Location getDestination();
}
