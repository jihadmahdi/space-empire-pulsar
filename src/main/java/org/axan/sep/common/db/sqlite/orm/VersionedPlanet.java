package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.VersionedProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.Planet;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedPlanet;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public class VersionedPlanet implements IVersionedPlanet
{
	private final VersionedProductiveCelestialBody versionedProductiveCelestialBodyProxy;
	private final Planet planetProxy;
	private final BaseVersionedPlanet baseVersionedPlanetProxy;

	public VersionedPlanet(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, Integer turn, String owner, Integer carbonStock, Integer currentCarbon, Integer populationPerTurn, Integer maxPopulation, Integer currentPopulation)
	{
		versionedProductiveCelestialBodyProxy = new VersionedProductiveCelestialBody(name, type, location, initialCarbonStock, maxSlots, turn, owner, carbonStock, currentCarbon);
		planetProxy = new Planet(name, type, location, initialCarbonStock, maxSlots, populationPerTurn, maxPopulation);
		baseVersionedPlanetProxy = new BaseVersionedPlanet(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, turn, owner, carbonStock, currentCarbon, populationPerTurn, maxPopulation, currentPopulation);
	}

	public VersionedPlanet(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.versionedProductiveCelestialBodyProxy = new VersionedProductiveCelestialBody(stmnt, config);
		this.planetProxy = new Planet(stmnt, config);
		this.baseVersionedPlanetProxy = new BaseVersionedPlanet(stmnt);
	}

	public Integer getCurrentPopulation()
	{
		return baseVersionedPlanetProxy.getCurrentPopulation();
	}

	public Integer getTurn()
	{
		return versionedProductiveCelestialBodyProxy.getTurn();
	}

	public String getOwner()
	{
		return versionedProductiveCelestialBodyProxy.getOwner();
	}

	public Integer getCarbonStock()
	{
		return versionedProductiveCelestialBodyProxy.getCarbonStock();
	}

	public Integer getCurrentCarbon()
	{
		return versionedProductiveCelestialBodyProxy.getCurrentCarbon();
	}

	public Integer getInitialCarbonStock()
	{
		return versionedProductiveCelestialBodyProxy.getInitialCarbonStock();
	}

	public Integer getMaxSlots()
	{
		return versionedProductiveCelestialBodyProxy.getMaxSlots();
	}

	public String getName()
	{
		return versionedProductiveCelestialBodyProxy.getName();
	}

	public eCelestialBodyType getType()
	{
		return versionedProductiveCelestialBodyProxy.getType();
	}

	public Location getLocation()
	{
		return versionedProductiveCelestialBodyProxy.getLocation();
	}

	public Integer getPopulationPerTurn()
	{
		return planetProxy.getPopulationPerTurn();
	}

	public Integer getMaxPopulation()
	{
		return planetProxy.getMaxPopulation();
	}

}
