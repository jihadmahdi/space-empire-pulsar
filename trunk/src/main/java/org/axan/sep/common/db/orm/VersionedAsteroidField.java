package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedAsteroidField;
import org.axan.sep.common.db.orm.base.BaseVersionedAsteroidField;
import org.axan.sep.common.db.IVersionedAsteroidField;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedAsteroidField extends VersionedProductiveCelestialBody implements IVersionedAsteroidField
{
	private final IBaseVersionedAsteroidField baseVersionedAsteroidFieldProxy;

	VersionedAsteroidField(IBaseVersionedAsteroidField baseVersionedAsteroidFieldProxy)
	{
		super(baseVersionedAsteroidFieldProxy);
		this.baseVersionedAsteroidFieldProxy = baseVersionedAsteroidFieldProxy;
	}

	public VersionedAsteroidField(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, Integer turn, String owner, Integer carbonStock, Integer currentCarbon)
	{
		this(new BaseVersionedAsteroidField(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, turn, owner, carbonStock, currentCarbon));
	}

	public VersionedAsteroidField(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseVersionedAsteroidField(stmnt));
	}

}
