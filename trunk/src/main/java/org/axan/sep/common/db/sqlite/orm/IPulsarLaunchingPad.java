package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IBuilding;
import org.axan.sep.common.db.sqlite.orm.base.IBasePulsarLaunchingPad;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IPulsarLaunchingPad extends IBuilding
{
	public Integer getFiredDate();
}
