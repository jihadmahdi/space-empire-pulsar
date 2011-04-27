package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.IProductiveCelestialBody;
import org.axan.sep.server.model.orm.base.IBasePlanet;

public interface IPlanet extends IProductiveCelestialBody
{
	public Integer getMaxPopulation();
	public Integer getPopulationPerTurn();
}
