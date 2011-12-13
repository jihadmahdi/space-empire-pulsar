package org.axan.sep.common.db;

import java.util.Map;

public interface ISpaceRoad
{
	public String getName();
	public String getBuilder();
	public String getSpaceCounterAType();
	public String getSpaceCounterACelestialBodyName();
	public Integer getSpaceCounterATurn();
	public String getSpaceCounterBType();
	public String getSpaceCounterBCelestialBodyName();
	public Integer getSpaceCounterBTurn();
	public Map<String, Object> getNode();
}
