package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IUnit;
import org.axan.sep.server.model.orm.base.IBasePulsarMissile;

public interface IPulsarMissile extends IUnit
{
	public Integer getVolume();
	public Integer getTime();
}
