package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.VersionedProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.Nebula;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedNebula;
import org.axan.sep.common.db.IVersionedNebula;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedNebula implements IVersionedNebula
{
	private final VersionedProductiveCelestialBody versionedProductiveCelestialBodyProxy;
	private final Nebula nebulaProxy;
	private final BaseVersionedNebula baseVersionedNebulaProxy;

	public VersionedNebula(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, Integer turn, String owner, Integer carbonStock, Integer currentCarbon)
	{
		versionedProductiveCelestialBodyProxy = new VersionedProductiveCelestialBody(name, type, location, initialCarbonStock, maxSlots, turn, owner, carbonStock, currentCarbon);
		nebulaProxy = new Nebula(name, type, location, initialCarbonStock, maxSlots);
		baseVersionedNebulaProxy = new BaseVersionedNebula(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, turn, owner, carbonStock, currentCarbon);
	}

	public VersionedNebula(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.versionedProductiveCelestialBodyProxy = new VersionedProductiveCelestialBody(stmnt, config);
		this.nebulaProxy = new Nebula(stmnt, config);
		this.baseVersionedNebulaProxy = new BaseVersionedNebula(stmnt);
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

}
