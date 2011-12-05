package org.axan.sep.common.db;


public interface IPlanet extends IProductiveCelestialBody
{
	public Integer getPopulationPerTurn();
	public Integer getMaxPopulation();
	public Integer getCurrentPopulation();
}
