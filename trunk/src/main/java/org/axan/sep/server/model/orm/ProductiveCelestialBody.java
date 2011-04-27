package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.CelestialBody;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseProductiveCelestialBody;

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
