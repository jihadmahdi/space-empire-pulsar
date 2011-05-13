package org.axan.sep.common.db;


public interface IVersionedCarbonCarrier extends ICarbonCarrier, IVersionedUnit
{
	public String getOrderOwner();
	public String getOrderSource();
	public Integer getOrderPriority();
}
