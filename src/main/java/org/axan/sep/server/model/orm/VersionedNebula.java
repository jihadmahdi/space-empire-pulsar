package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.VersionedProductiveCelestialBody;
import org.axan.sep.server.model.orm.Nebula;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseVersionedNebula;

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

	public Integer getLocation_y()
	{
		return versionedProductiveCelestialBodyProxy.getLocation_y();
	}

	public Integer getLocation_x()
	{
		return versionedProductiveCelestialBodyProxy.getLocation_x();
	}

	public String getType()
	{
		return versionedProductiveCelestialBodyProxy.getType();
	}

	public String getName()
	{
		return versionedProductiveCelestialBodyProxy.getName();
	}

	public Integer getLocation_z()
	{
		return versionedProductiveCelestialBodyProxy.getLocation_z();
	}

}
