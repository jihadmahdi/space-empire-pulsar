package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IUnit;
import org.axan.sep.server.model.orm.base.IBaseVersionedUnit;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IVersionedUnit extends IUnit
{
	public Location getDeparture();
	public Double getProgress();
	public Location getDestination();
	public Integer getTurn();
}
