package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IBuilding;
import org.axan.sep.server.model.orm.base.IBasePulsarLaunchingPad;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IPulsarLaunchingPad extends IBuilding
{
	public Integer getFiredDate();
}
