package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.INebula;
import org.axan.sep.common.db.orm.base.BaseNebula;
import org.axan.sep.common.db.orm.base.IBaseNebula;

public class Nebula extends ProductiveCelestialBody implements INebula
{
	private final IBaseNebula baseNebulaProxy;

	Nebula(IBaseNebula baseNebulaProxy)
	{
		super(baseNebulaProxy);
		this.baseNebulaProxy = baseNebulaProxy;
	}

	public Nebula(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, String owner, Integer carbonStock, Integer currentCarbon)
	{
		this(new BaseNebula(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, owner, carbonStock, currentCarbon));
	}

	public Nebula(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseNebula(stmnt));
	}

}
