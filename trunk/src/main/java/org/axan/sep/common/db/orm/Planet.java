package org.axan.sep.common.db.orm;

import java.io.Serializable;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IPlanet;
import org.axan.sep.common.db.SEPCommonDB;
import org.axan.sep.common.db.orm.base.BasePlanet;
import org.axan.sep.common.db.orm.base.IBasePlanet;

public class Planet extends ProductiveCelestialBody implements IPlanet, Serializable
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

	/**
	 * Return the starting planet on first turn. Actually return the first planet owned by given player.
	 * @param playerName
	 * @return
	 * @throws SQLDataBaseException 
	 */
	public static IPlanet getStartingPlanet(SEPCommonDB db, String playerName) throws SQLDataBaseException
	{
		return ProductiveCelestialBody.selectOne(db, IPlanet.class, null, "owner = ?", playerName);
	}
}
