package org.axan.sep.common.db.orm;

import java.io.Serializable;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseCelestialBody;
import org.axan.sep.common.db.orm.base.BaseCelestialBody;
import org.axan.sep.common.db.ICelestialBody;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class CelestialBody implements ICelestialBody, Serializable
{
	private final IBaseCelestialBody baseCelestialBodyProxy;
	private final eCelestialBodyType type;
	private final Location location;

	CelestialBody(IBaseCelestialBody baseCelestialBodyProxy)
	{
		this.baseCelestialBodyProxy = baseCelestialBodyProxy;
		this.type = eCelestialBodyType.valueOf(baseCelestialBodyProxy.getType());
		this.location = (baseCelestialBodyProxy.getLocation_x() == null ? null : new Location(baseCelestialBodyProxy.getLocation_x(), baseCelestialBodyProxy.getLocation_y(), baseCelestialBodyProxy.getLocation_z()));
	}

	public CelestialBody(String name, eCelestialBodyType type, Location location)
	{
		this(new BaseCelestialBody(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z));
	}

	public CelestialBody(Node stmnt) throws Exception
	{
		this(new BaseCelestialBody(stmnt));
	}

	@Override
	public String getName()
	{
		return baseCelestialBodyProxy.getName();
	}

	@Override
	public eCelestialBodyType getType()
	{
		return type;
	}

	@Override
	public Location getLocation()
	{
		return location;
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseCelestialBodyProxy.getNode();
	}

}
