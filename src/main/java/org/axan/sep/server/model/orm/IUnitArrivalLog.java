package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseUnitArrivalLog;

public interface IUnitArrivalLog
{
	public String getUnitType();
	public Integer getUnitTurn();
	public String getDestination();
	public String getUnitName();
	public Integer getInstantTime();
	public String getUnitOwner();
	public String getVortex();
}
