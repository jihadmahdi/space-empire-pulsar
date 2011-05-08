package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseAsteroidField;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public class AsteroidField extends ProductiveCelestialBody implements IAsteroidField
{
	private final BaseAsteroidField baseAsteroidFieldProxy;

	public AsteroidField(String name, eCelestialBodyType type, Location location, Integer initialCarbonStock, Integer maxSlots)
	{
		super(name, type, location, initialCarbonStock, maxSlots);
		baseAsteroidFieldProxy = new BaseAsteroidField(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, initialCarbonStock, maxSlots);
	}

	public AsteroidField(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseAsteroidFieldProxy = new BaseAsteroidField(stmnt);
	}

}
