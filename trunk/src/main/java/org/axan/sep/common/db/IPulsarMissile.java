package org.axan.sep.common.db;

import org.axan.sep.common.SEPUtils.Location;


public interface IPulsarMissile extends IUnit
{
	public Integer getTime();
	public Integer getVolume();
	public Location getDirection();
}
