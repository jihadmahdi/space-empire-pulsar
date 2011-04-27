package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IVersionedProductiveCelestialBody;
import org.axan.sep.server.model.orm.IPlanet;
import org.axan.sep.server.model.orm.base.IBaseVersionedPlanet;

public interface IVersionedPlanet extends IVersionedProductiveCelestialBody, IPlanet
{
	public Integer getCurrentPopulation();
}
