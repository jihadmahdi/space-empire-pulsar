package org.axan.sep.common.db;

import java.util.Map;

public interface IFleetComposition
{
	public String getFleetOwner();
	public String getFleetName();
	public String getStarshipTemplate();
	public Integer getQuantity();
	public Map<String, Object> getNode();
}
