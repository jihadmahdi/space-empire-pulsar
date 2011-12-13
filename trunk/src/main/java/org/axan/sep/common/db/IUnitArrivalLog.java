package org.axan.sep.common.db;

import java.util.Map;

public interface IUnitArrivalLog
{
	public String getOwner();
	public String getUnitName();
	public String getUnitType();
	public Integer getLogTurn();
	public Integer getInstantTime();
	public String getDestination();
	public String getVortex();
	public Map<String, Object> getNode();
}
