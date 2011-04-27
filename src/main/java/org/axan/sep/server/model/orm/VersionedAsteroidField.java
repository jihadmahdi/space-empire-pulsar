package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.AsteroidField;
import org.axan.sep.server.model.orm.VersionedProductiveCelestialBody;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseVersionedAsteroidField;

public class VersionedAsteroidField implements IVersionedAsteroidField
{
	private final AsteroidField asteroidFieldProxy;
	private final VersionedProductiveCelestialBody versionedProductiveCelestialBodyProxy;
	private final BaseVersionedAsteroidField baseVersionedAsteroidFieldProxy;

	public VersionedAsteroidField(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.asteroidFieldProxy = new AsteroidField(stmnt, config);
		this.versionedProductiveCelestialBodyProxy = new VersionedProductiveCelestialBody(stmnt, config);
		this.baseVersionedAsteroidFieldProxy = new BaseVersionedAsteroidField(stmnt);
	}

	public Integer getInitialCarbonStock()
	{
		return asteroidFieldProxy.getInitialCarbonStock();
	}

	public Integer getMaxSlots()
	{
		return asteroidFieldProxy.getMaxSlots();
	}

	public Integer getLocation_y()
	{
		return asteroidFieldProxy.getLocation_y();
	}

	public Integer getLocation_x()
	{
		return asteroidFieldProxy.getLocation_x();
	}

	public String getType()
	{
		return asteroidFieldProxy.getType();
	}

	public String getName()
	{
		return asteroidFieldProxy.getName();
	}

	public Integer getLocation_z()
	{
		return asteroidFieldProxy.getLocation_z();
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

}
