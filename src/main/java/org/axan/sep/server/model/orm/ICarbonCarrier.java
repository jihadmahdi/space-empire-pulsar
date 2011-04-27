package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IUnit;
import org.axan.sep.server.model.orm.base.IBaseCarbonCarrier;

public interface ICarbonCarrier extends IUnit
{
	public String getSourceCelestialBodyName();
	public Integer getSourceTurn();
	public String getSourceType();
}
