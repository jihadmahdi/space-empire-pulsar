package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.AsteroidField;
import org.axan.sep.common.db.sqlite.orm.VersionedProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedAsteroidField;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

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

	public Location getLocation()
	{
		return asteroidFieldProxy.getLocation();
	}

	public eCelestialBodyType getType()
	{
		return asteroidFieldProxy.getType();
	}

	public String getName()
	{
		return asteroidFieldProxy.getName();
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
