package org.axan.sep.common.db;

import org.axan.sep.common.Protocol.eUnitType;

public interface IUnit
{
	public String getOwner();
	public String getName();
	public eUnitType getType();
	public float getSight();
}
