package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.ICelestialBody;
import org.axan.sep.server.model.orm.base.IBaseVortex;

public interface IVortex extends ICelestialBody
{
	public Integer getOnsetDate();
	public Integer getEndDate();
	public String getDestination();
}
