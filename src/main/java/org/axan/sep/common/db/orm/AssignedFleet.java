package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseAssignedFleet;
import org.axan.sep.common.db.orm.base.BaseAssignedFleet;
import org.axan.sep.common.db.IAssignedFleet;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class AssignedFleet implements IAssignedFleet
{
	private final IBaseAssignedFleet baseAssignedFleetProxy;

	AssignedFleet(IBaseAssignedFleet baseAssignedFleetProxy)
	{
		this.baseAssignedFleetProxy = baseAssignedFleetProxy;
	}

	public AssignedFleet(String celestialBody, String owner, String fleetName)
	{
		this(new BaseAssignedFleet(celestialBody, owner, fleetName));
	}

	public AssignedFleet(Node stmnt) throws Exception
	{
		this(new BaseAssignedFleet(stmnt));
	}

	@Override
	public String getCelestialBody()
	{
		return baseAssignedFleetProxy.getCelestialBody();
	}

	@Override
	public String getOwner()
	{
		return baseAssignedFleetProxy.getOwner();
	}

	@Override
	public String getFleetName()
	{
		return baseAssignedFleetProxy.getFleetName();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseAssignedFleetProxy.getNode();
	}

}
