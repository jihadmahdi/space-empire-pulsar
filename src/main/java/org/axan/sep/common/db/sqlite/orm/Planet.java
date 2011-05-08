package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BasePlanet;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public class Planet extends ProductiveCelestialBody implements IPlanet
{
	private final BasePlanet basePlanetProxy;

	public Planet(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, Integer populationPerTurn, Integer maxPopulation)
	{
		super(name, type, location, initialCarbonStock, maxSlots);
		basePlanetProxy = new BasePlanet(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, populationPerTurn, maxPopulation);
	}

	public Planet(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.basePlanetProxy = new BasePlanet(stmnt);
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
