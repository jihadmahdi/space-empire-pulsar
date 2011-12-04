package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.ProductiveCelestialBody;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedProductiveCelestialBody;
import org.axan.sep.common.db.orm.base.BaseVersionedProductiveCelestialBody;
import org.axan.sep.common.db.IVersionedProductiveCelestialBody;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedProductiveCelestialBody extends ProductiveCelestialBody implements IVersionedProductiveCelestialBody
{
	private final IBaseVersionedProductiveCelestialBody baseVersionedProductiveCelestialBodyProxy;

	VersionedProductiveCelestialBody(IBaseVersionedProductiveCelestialBody baseVersionedProductiveCelestialBodyProxy)
	{
		super(baseVersionedProductiveCelestialBodyProxy);
		this.baseVersionedProductiveCelestialBodyProxy = baseVersionedProductiveCelestialBodyProxy;
	}

	public VersionedProductiveCelestialBody(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, Integer turn, String owner, Integer carbonStock, Integer currentCarbon)
	{
		this(new BaseVersionedProductiveCelestialBody(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, turn, owner, carbonStock, currentCarbon));
	}

	public VersionedProductiveCelestialBody(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseVersionedProductiveCelestialBody(stmnt));
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
