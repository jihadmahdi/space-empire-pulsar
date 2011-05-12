package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.base.BaseCelestialBody;
import org.axan.sep.common.db.ICelestialBody;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class CelestialBody implements ICelestialBody
{
	private final BaseCelestialBody baseCelestialBodyProxy;
	private eCelestialBodyType type;private Location location;

	public CelestialBody(String name, eCelestialBodyType type, Location location)
	{
		baseCelestialBodyProxy = new BaseCelestialBody(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z);
		this.type = type;
		this.location = location;
	}

	public CelestialBody(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseCelestialBodyProxy = new BaseCelestialBody(stmnt);
		this.type = eCelestialBodyType.valueOf(baseCelestialBodyProxy.getType());
		this.location = (baseCelestialBodyProxy.getLocation_x() == null ? null : new Location(baseCelestialBodyProxy.getLocation_x(), baseCelestialBodyProxy.getLocation_y(), baseCelestialBodyProxy.getLocation_z()));
	}

	public String getName()
	{
		return baseCelestialBodyProxy.getName();
	}

	public eCelestialBodyType getType()
	{
		return type;
	}

	public Location getLocation()
	{
		return location;
	}

}
