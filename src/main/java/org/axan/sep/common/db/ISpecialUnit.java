package org.axan.sep.common.db;

import org.axan.sep.common.Protocol.eSpecialUnitType;

public interface ISpecialUnit
{
	public String getOwner();
	public String getName();
	public eSpecialUnitType getType();
}
