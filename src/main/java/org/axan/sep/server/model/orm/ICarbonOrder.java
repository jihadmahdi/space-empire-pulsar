package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseCarbonOrder;

public interface ICarbonOrder
{
	public String getSource();
	public Integer getAmount();
	public Integer getPriority();
	public String getOwner();
	public String getDestination();
}
