package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.orm.base.BasePlanet;
import org.axan.sep.common.db.orm.base.IBasePlanet;

public class Planet extends ProductiveCelestialBody implements IPlanet
{
	private final IBasePlanet basePlanetProxy;

	Planet(IBasePlanet basePlanetProxy)
	{
		super(basePlanetProxy);
		this.basePlanetProxy = basePlanetProxy;
	}

	public Planet(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, String owner, Integer carbonStock, Integer currentCarbon, Integer populationPerTurn, Integer maxPopulation, Integer currentPopulation)
	{
		this(new BasePlanet(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, owner, carbonStock, currentCarbon, populationPerTurn, maxPopulation, currentPopulation));
	}

	public Planet(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BasePlanet(stmnt));
	}

	@Override
	public Integer getPopulationPerTurn()
	{
		return basePlanetProxy.getPopulationPerTurn();
	}

	@Override
	public Integer getMaxPopulation()
	{
		return basePlanetProxy.getMaxPopulation();
	}

	@Override
	public Integer getCurrentPopulation()
	{
		return basePlanetProxy.getCurrentPopulation();
	}

}
