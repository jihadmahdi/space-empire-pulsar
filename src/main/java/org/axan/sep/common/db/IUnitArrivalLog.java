package org.axan.sep.common.db;


public interface IUnitArrivalLog
{
	public String getOwner();
	public String getUnitName();
	public String getUnitType();
	public Integer getLogTurn();
	public Integer getInstantTime();
	public String getDestination();
	public String getVortex();
}
