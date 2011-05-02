package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.VersionedProductiveCelestialBody;
import org.axan.sep.server.model.orm.Planet;
import org.axan.sep.server.model.orm.base.BaseVersionedPlanet;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import com.almworks.sqlite4java.SQLiteStatement;

public class VersionedPlanet implements IVersionedPlanet
{
	private final VersionedProductiveCelestialBody versionedProductiveCelestialBodyProxy;
	private final Planet planetProxy;
	private final BaseVersionedPlanet baseVersionedPlanetProxy;

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

	public Integer getCurrentCarbon()
	{
		return versionedProductiveCelestialBodyProxy.getCurrentCarbon();
	}

	public String getOwner()
	{
		return versionedProductiveCelestialBodyProxy.getOwner();
	}

	public Integer getCarbonStock()
	{
		return versionedProductiveCelestialBodyProxy.getCarbonStock();
	}

	public Integer getTurn()
	{
		return versionedProductiveCelestialBodyProxy.getTurn();
	}

	public Integer getInitialCarbonStock()
	{
		return versionedProductiveCelestialBodyProxy.getInitialCarbonStock();
	}

	public Integer getMaxSlots()
	{
		return versionedProductiveCelestialBodyProxy.getMaxSlots();
	}

	public Location getLocation()
	{
		return versionedProductiveCelestialBodyProxy.getLocation();
	}

	public eCelestialBodyType getType()
	{
		return versionedProductiveCelestialBodyProxy.getType();
	}

	public String getName()
	{
		return versionedProductiveCelestialBodyProxy.getName();
	}

	public Integer getMaxPopulation()
	{
		return planetProxy.getMaxPopulation();
	}

	public Integer getPopulationPerTurn()
	{
		return planetProxy.getPopulationPerTurn();
	}

}
