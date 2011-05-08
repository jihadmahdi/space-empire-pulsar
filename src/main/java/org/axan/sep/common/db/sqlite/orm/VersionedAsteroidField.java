package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.AsteroidField;
import org.axan.sep.common.db.sqlite.orm.VersionedProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedAsteroidField;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public class VersionedAsteroidField implements IVersionedAsteroidField
{
	private final AsteroidField asteroidFieldProxy;
	private final VersionedProductiveCelestialBody versionedProductiveCelestialBodyProxy;
	private final BaseVersionedAsteroidField baseVersionedAsteroidFieldProxy;

	public VersionedAsteroidField(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, Integer turn, String owner, Integer carbonStock, Integer currentCarbon)
	{
		asteroidFieldProxy = new AsteroidField(name, type, location, initialCarbonStock, maxSlots);
		versionedProductiveCelestialBodyProxy = new VersionedProductiveCelestialBody(name, type, location, initialCarbonStock, maxSlots, turn, owner, carbonStock, currentCarbon);
		baseVersionedAsteroidFieldProxy = new BaseVersionedAsteroidField(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, turn, owner, carbonStock, currentCarbon);
	}

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

	public String getName()
	{
		return asteroidFieldProxy.getName();
	}

	public eCelestialBodyType getType()
	{
		return asteroidFieldProxy.getType();
	}

	public Location getLocation()
	{
		return asteroidFieldProxy.getLocation();
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

}
