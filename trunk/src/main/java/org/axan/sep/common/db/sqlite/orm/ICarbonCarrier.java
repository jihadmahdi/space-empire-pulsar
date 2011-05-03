package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBaseCarbonCarrier;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface ICarbonCarrier extends IUnit
{
	public String getSourceCelestialBodyName();
	public Integer getSourceTurn();
	public String getSourceType();
}
