package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.CelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseProductiveCelestialBody;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public class ProductiveCelestialBody extends CelestialBody implements IProductiveCelestialBody
{
	private final BaseProductiveCelestialBody baseProductiveCelestialBodyProxy;

	public ProductiveCelestialBody(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots)
	{
		super(name, type, location);
		baseProductiveCelestialBodyProxy = new BaseProductiveCelestialBody(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots);
	}

	public ProductiveCelestialBody(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseProductiveCelestialBodyProxy = new BaseProductiveCelestialBody(stmnt);
	}

	public Integer getInitialCarbonStock()
	{
		return baseProductiveCelestialBodyProxy.getInitialCarbonStock();
	}

	public Integer getMaxSlots()
	{
		return baseProductiveCelestialBodyProxy.getMaxSlots();
	}

}
