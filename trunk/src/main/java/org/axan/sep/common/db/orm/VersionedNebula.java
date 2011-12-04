package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedNebula;
import org.axan.sep.common.db.orm.base.BaseVersionedNebula;
import org.axan.sep.common.db.IVersionedNebula;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedNebula extends VersionedProductiveCelestialBody implements IVersionedNebula
{
	private final IBaseVersionedNebula baseVersionedNebulaProxy;

	VersionedNebula(IBaseVersionedNebula baseVersionedNebulaProxy)
	{
		super(baseVersionedNebulaProxy);
		this.baseVersionedNebulaProxy = baseVersionedNebulaProxy;
	}

	public VersionedNebula(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, Integer turn, String owner, Integer carbonStock, Integer currentCarbon)
	{
		this(new BaseVersionedNebula(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, turn, owner, carbonStock, currentCarbon));
	}

	public VersionedNebula(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseVersionedNebula(stmnt));
	}

}
