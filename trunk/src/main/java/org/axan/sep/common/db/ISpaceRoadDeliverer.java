package org.axan.sep.common.db;

import org.axan.sep.common.db.IUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;

public interface ISpaceRoadDeliverer extends IUnit
{
	public String getSourceType();
	public String getSourceCelestialBodyName();
	public Integer getSourceTurn();
	public String getDestinationType();
	public String getDestinationCelestialBodyName();
	public Integer getDestinationTurn();
}
