package org.axan.sep.common.db;

import java.util.Map;

public interface IUnitEncounterLog
{
	public String getOwner();
	public String getUnitName();
	public String getUnitType();
	public Integer getLogTurn();
	public Integer getInstantTime();
	public String getSeenOwner();
	public String getSeenName();
	public Integer getSeenTurn();
	public String getSeenType();
	public Map<String, Object> getNode();
}
