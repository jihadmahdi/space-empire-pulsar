package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedPlanet;
import org.axan.sep.common.db.orm.base.BaseVersionedPlanet;
import org.axan.sep.common.db.IVersionedPlanet;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedPlanet extends VersionedProductiveCelestialBody implements IVersionedPlanet
{
	private final IBaseVersionedPlanet baseVersionedPlanetProxy;

	VersionedPlanet(IBaseVersionedPlanet baseVersionedPlanetProxy)
	{
		super(baseVersionedPlanetProxy);
		this.baseVersionedPlanetProxy = baseVersionedPlanetProxy;
	}

	public VersionedPlanet(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, Integer turn, String owner, Integer carbonStock, Integer currentCarbon, Integer populationPerTurn, Integer maxPopulation, Integer currentPopulation)
	{
		this(new BaseVersionedPlanet(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, turn, owner, carbonStock, currentCarbon, populationPerTurn, maxPopulation, currentPopulation));
	}

	public VersionedPlanet(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseVersionedPlanet(stmnt));
	}

	public Integer getCurrentPopulation()
	{
		return baseVersionedPlanetProxy.getCurrentPopulation();
	}

	public Integer getPopulationPerTurn()
	{
		return baseVersionedPlanetProxy.getPopulationPerTurn();
	}

	public Integer getMaxPopulation()
	{
		return baseVersionedPlanetProxy.getMaxPopulation();
	}

}
