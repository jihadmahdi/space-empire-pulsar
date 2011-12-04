package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.ProductiveCelestialBody;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseAsteroidField;
import org.axan.sep.common.db.orm.base.BaseAsteroidField;
import org.axan.sep.common.db.IAsteroidField;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class AsteroidField extends ProductiveCelestialBody implements IAsteroidField
{
	private final IBaseAsteroidField baseAsteroidFieldProxy;

	AsteroidField(IBaseAsteroidField baseAsteroidFieldProxy)
	{
		super(baseAsteroidFieldProxy);
		this.baseAsteroidFieldProxy = baseAsteroidFieldProxy;
	}

	public AsteroidField(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots)
	{
		this(new BaseAsteroidField(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots));
	}

	public AsteroidField(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseAsteroidField(stmnt));
	}

}
