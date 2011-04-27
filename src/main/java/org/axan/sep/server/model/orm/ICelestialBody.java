package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseCelestialBody;

public interface ICelestialBody
{
	public Integer getLocation_y();
	public Integer getLocation_x();
	public String getType();
	public String getName();
	public Integer getLocation_z();
}
