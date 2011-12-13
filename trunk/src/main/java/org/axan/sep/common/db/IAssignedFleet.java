package org.axan.sep.common.db;

import java.util.Map;

public interface IAssignedFleet
{
	public String getCelestialBody();
	public String getOwner();
	public String getFleetName();
	public Map<String, Object> getNode();
}
