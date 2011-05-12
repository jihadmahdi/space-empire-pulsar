package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseNebula;
import org.axan.sep.common.db.INebula;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class Nebula extends ProductiveCelestialBody implements INebula
{
	private final BaseNebula baseNebulaProxy;

	public Nebula(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots)
	{
		super(name, type, location, initialCarbonStock, maxSlots);
		baseNebulaProxy = new BaseNebula(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots);
	}

	public Nebula(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseNebulaProxy = new BaseNebula(stmnt);
	}

}
