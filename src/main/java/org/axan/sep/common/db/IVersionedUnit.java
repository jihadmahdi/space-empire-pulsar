package org.axan.sep.common.db;

import org.axan.sep.common.db.IUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public interface IVersionedUnit extends IUnit
{
	public Integer getTurn();
	public Location getDeparture();
	public Double getProgress();
	public Location getDestination();
}
