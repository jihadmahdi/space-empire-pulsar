package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.ICarbonCarrier;
import org.axan.sep.server.model.orm.IVersionedUnit;
import org.axan.sep.server.model.orm.base.IBaseVersionedCarbonCarrier;

public interface IVersionedCarbonCarrier extends ICarbonCarrier, IVersionedUnit
{
	public String getOrderOwner();
	public String getOrderSource();
	public Integer getOrderPriority();
}
