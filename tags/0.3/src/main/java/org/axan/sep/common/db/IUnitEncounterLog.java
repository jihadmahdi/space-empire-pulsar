package org.axan.sep.common.db;


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
}
