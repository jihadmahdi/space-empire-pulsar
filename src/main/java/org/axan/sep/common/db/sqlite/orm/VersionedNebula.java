package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Nebula;
import org.axan.sep.common.db.sqlite.orm.VersionedProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedNebula;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public class VersionedNebula implements IVersionedNebula
{
	private final VersionedProductiveCelestialBody versionedProductiveCelestialBodyProxy;
	private final Nebula nebulaProxy;
	private final BaseVersionedNebula baseVersionedNebulaProxy;

	public VersionedNebula(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.versionedProductiveCelestialBodyProxy = new VersionedProductiveCelestialBody(stmnt, config);
		this.nebulaProxy = new Nebula(stmnt, config);
		this.baseVersionedNebulaProxy = new BaseVersionedNebula(stmnt);
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

}
