package org.axan.sep.common.db.orm;

import java.io.Serializable;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IAsteroidField;
import org.axan.sep.common.db.orm.base.BaseAsteroidField;
import org.axan.sep.common.db.orm.base.IBaseAsteroidField;

public class AsteroidField extends ProductiveCelestialBody implements IAsteroidField, Serializable
{
	private final IBaseAsteroidField baseAsteroidFieldProxy;

	AsteroidField(IBaseAsteroidField baseAsteroidFieldProxy)
	{
		super(baseAsteroidFieldProxy);
		this.baseAsteroidFieldProxy = baseAsteroidFieldProxy;
	}

	public AsteroidField(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots, String owner, Integer carbonStock, Integer currentCarbon)
	{
		this(new BaseAsteroidField(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots, owner, carbonStock, currentCarbon));
	}

	public AsteroidField(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseAsteroidField(stmnt));
	}

}
