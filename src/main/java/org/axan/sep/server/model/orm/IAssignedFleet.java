package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseAssignedFleet;

public interface IAssignedFleet
{
	public String getFleetName();
	public String getOwner();
	public String getCelestialBody();
}
