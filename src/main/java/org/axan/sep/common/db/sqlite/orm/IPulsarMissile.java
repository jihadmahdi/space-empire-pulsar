package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBasePulsarMissile;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IPulsarMissile extends IUnit
{
	public Integer getVolume();
	public Integer getTime();
}
