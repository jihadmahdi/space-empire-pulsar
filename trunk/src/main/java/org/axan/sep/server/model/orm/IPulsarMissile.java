package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IUnit;
import org.axan.sep.server.model.orm.base.IBasePulsarMissile;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IPulsarMissile extends IUnit
{
	public Integer getVolume();
	public Integer getTime();
}
