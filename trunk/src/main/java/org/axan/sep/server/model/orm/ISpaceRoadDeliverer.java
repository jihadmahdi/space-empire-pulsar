package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IUnit;
import org.axan.sep.server.model.orm.base.IBaseSpaceRoadDeliverer;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface ISpaceRoadDeliverer extends IUnit
{
	public String getDestinationType();
	public Integer getDestinationTurn();
	public String getSourceCelestialBodyName();
	public Integer getSourceTurn();
	public String getSourceType();
	public String getDestinationCelestialBodyName();
}
