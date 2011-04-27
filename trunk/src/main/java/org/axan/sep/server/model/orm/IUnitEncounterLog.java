package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseUnitEncounterLog;

public interface IUnitEncounterLog
{
	public Integer getSeenTurn();
	public String getSeenOwner();
	public String getUnitType();
	public Integer getUnitTurn();
	public String getSeenType();
	public String getUnitName();
	public Integer getInstantTime();
	public String getUnitOwner();
	public String getSeenName();
}
