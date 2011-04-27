package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IBuilding;
import org.axan.sep.server.model.orm.base.IBasePulsarLaunchingPad;

public interface IPulsarLaunchingPad extends IBuilding
{
	public Integer getFiredDate();
}
