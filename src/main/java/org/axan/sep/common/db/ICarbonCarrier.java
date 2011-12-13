package org.axan.sep.common.db;

import java.util.Map;

public interface ICarbonCarrier extends IUnit
{
	public String getSourceType();
	public String getSourceCelestialBodyName();
	public Integer getSourceTurn();
	public String getOrderOwner();
	public String getOrderSource();
	public Integer getOrderPriority();
	public Map<String, Object> getNode();
}
