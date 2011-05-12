package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedProductiveCelestialBody;
import org.axan.sep.common.db.IVersionedProductiveCelestialBody;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedProductiveCelestialBody extends ProductiveCelestialBody implements IVersionedProductiveCelestialBody
{
	private final BaseVersionedProductiveCelestialBody baseVersionedProductiveCelestialBodyProxy;

	public VersionedProductiveCelestialBody(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, Integer turn, String owner, Integer carbonStock, Integer currentCarbon)
	{
		super(name, type, location, initialCarbonStock, maxSlots);
		baseVersionedProductiveCelestialBodyProxy = new BaseVersionedProductiveCelestialBody(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, turn, owner, carbonStock, currentCarbon);
	}

	public VersionedProductiveCelestialBody(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseVersionedProductiveCelestialBodyProxy = new BaseVersionedProductiveCelestialBody(stmnt);
	}

	public Integer getTurn()
	{
		return baseVersionedProductiveCelestialBodyProxy.getTurn();
	}

	public String getOwner()
	{
		return baseVersionedProductiveCelestialBodyProxy.getOwner();
	}

	public Integer getCarbonStock()
	{
		return baseVersionedProductiveCelestialBodyProxy.getCarbonStock();
	}

	public Integer getCurrentCarbon()
	{
		return baseVersionedProductiveCelestialBodyProxy.getCurrentCarbon();
	}

}
