package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.CelestialBody;

import java.io.Serializable;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVortex;
import org.axan.sep.common.db.orm.base.BaseVortex;
import org.axan.sep.common.db.IVortex;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class Vortex extends CelestialBody implements IVortex, Serializable
{
	private final IBaseVortex baseVortexProxy;

	Vortex(IBaseVortex baseVortexProxy)
	{
		super(baseVortexProxy);
		this.baseVortexProxy = baseVortexProxy;
	}

	public Vortex(String name, eCelestialBodyType type, Location location, Integer onsetDate, Integer endDate, String destination)
	{
		this(new BaseVortex(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, onsetDate, endDate, destination));
	}

	public Vortex(Node stmnt) throws Exception
	{
		this(new BaseVortex(stmnt));
	}

	@Override
	public Integer getOnsetDate()
	{
		return baseVortexProxy.getOnsetDate();
	}

	@Override
	public Integer getEndDate()
	{
		return baseVortexProxy.getEndDate();
	}

	@Override
	public String getDestination()
	{
		return baseVortexProxy.getDestination();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseVortexProxy.getNode();
	}

}
