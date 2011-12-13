package org.axan.sep.common.db;

import java.util.Map;

public interface ICarbonOrder
{
	public String getOwner();
	public String getSource();
	public Integer getPriority();
	public Integer getAmount();
	public String getDestination();
	public Map<String, Object> getNode();
}
