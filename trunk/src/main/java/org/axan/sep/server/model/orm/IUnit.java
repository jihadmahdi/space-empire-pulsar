package org.axan.sep.server.model.orm;

import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.server.model.orm.base.IBaseUnit;

public interface IUnit
{
	public String getOwner();
	public eUnitType getType();
	public String getName();
	float getSpeed();
	float getSight();
}
