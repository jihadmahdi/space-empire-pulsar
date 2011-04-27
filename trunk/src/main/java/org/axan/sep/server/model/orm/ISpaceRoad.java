package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseSpaceRoad;

public interface ISpaceRoad
{
	public String getSpaceCounterBCelestialBodyName();
	public Integer getSpaceCounterATurn();
	public String getSpaceCounterAType();
	public String getSpaceCounterBType();
	public Integer getSpaceCounterBTurn();
	public String getSpaceCounterACelestialBodyName();
}
