package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.ProductiveCelestialBody;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBasePlanet;
import org.axan.sep.common.db.orm.base.BasePlanet;
import org.axan.sep.common.db.IPlanet;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class Planet extends ProductiveCelestialBody implements IPlanet
{
	private final IBasePlanet basePlanetProxy;

	Planet(IBasePlanet basePlanetProxy)
	{
		super(basePlanetProxy);
		this.basePlanetProxy = basePlanetProxy;
	}

	public Planet(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, Integer populationPerTurn, Integer maxPopulation)
	{
		this(new BasePlanet(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, populationPerTurn, maxPopulation));
	}

	public Planet(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BasePlanet(stmnt));
	}

	public Integer getPopulationPerTurn()
	{
		return basePlanetProxy.getPopulationPerTurn();
	}

	public Integer getMaxPopulation()
	{
		return basePlanetProxy.getMaxPopulation();
	}

}
