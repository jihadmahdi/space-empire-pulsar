package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBasePulsarMissile;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;

public interface IPulsarMissile extends IUnit
{
	public Integer getTime();
	public Integer getVolume();
}
