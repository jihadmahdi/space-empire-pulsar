package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IProductiveCelestialBody;
import org.axan.sep.server.model.orm.base.IBaseVersionedProductiveCelestialBody;

public interface IVersionedProductiveCelestialBody extends IProductiveCelestialBody
{
	public Integer getCurrentCarbon();
	public String getOwner();
	public Integer getCarbonStock();
	public Integer getTurn();
}
