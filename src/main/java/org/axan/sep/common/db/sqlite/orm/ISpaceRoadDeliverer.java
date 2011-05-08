package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBaseSpaceRoadDeliverer;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;

public interface ISpaceRoadDeliverer extends IUnit
{
	public String getSourceType();
	public String getSourceCelestialBodyName();
	public Integer getSourceTurn();
	public String getDestinationType();
	public String getDestinationCelestialBodyName();
	public Integer getDestinationTurn();
}
