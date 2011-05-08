package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.IBuilding;
import org.axan.sep.common.db.sqlite.orm.base.IBasePulsarLaunchingPad;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eBuildingType;

public interface IPulsarLaunchingPad extends IBuilding
{
	public Integer getFiredDate();
}
