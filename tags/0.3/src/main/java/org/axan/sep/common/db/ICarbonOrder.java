package org.axan.sep.common.db;


public interface ICarbonOrder
{
	public String getOwner();
	public String getSource();
	public Integer getPriority();
	public Integer getAmount();
	public String getDestination();
}
