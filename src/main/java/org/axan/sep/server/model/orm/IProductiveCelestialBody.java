package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.ICelestialBody;
import org.axan.sep.server.model.orm.base.IBaseProductiveCelestialBody;

public interface IProductiveCelestialBody extends ICelestialBody
{
	public Integer getInitialCarbonStock();
	public Integer getMaxSlots();
}
