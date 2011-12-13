package org.axan.sep.common.db;

import java.util.Map;

public interface IGovernment
{
	public String getOwner();
	public String getFleetName();
	public String getPlanetName();
	public Map<String, Object> getNode();
}
