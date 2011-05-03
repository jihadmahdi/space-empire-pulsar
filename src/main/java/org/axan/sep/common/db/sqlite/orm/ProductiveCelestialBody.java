package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.CelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseProductiveCelestialBody;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class ProductiveCelestialBody extends CelestialBody implements IProductiveCelestialBody
{
	private final BaseProductiveCelestialBody baseProductiveCelestialBodyProxy;

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
