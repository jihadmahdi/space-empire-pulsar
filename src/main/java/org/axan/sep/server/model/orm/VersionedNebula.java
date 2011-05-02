package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.VersionedProductiveCelestialBody;
import org.axan.sep.server.model.orm.Nebula;
import org.axan.sep.server.model.orm.base.BaseVersionedNebula;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import com.almworks.sqlite4java.SQLiteStatement;

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
