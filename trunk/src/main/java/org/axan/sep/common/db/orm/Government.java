package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseGovernment;
import org.axan.sep.common.db.orm.base.BaseGovernment;
import org.axan.sep.common.db.IGovernment;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class Government implements IGovernment
{
	private final IBaseGovernment baseGovernmentProxy;

	Government(IBaseGovernment baseGovernmentProxy)
	{
		this.baseGovernmentProxy = baseGovernmentProxy;
	}

	public Government(String owner, String fleetName, String planetName)
	{
		this(new BaseGovernment(owner, fleetName, planetName));
	}

	public Government(Node stmnt) throws Exception
	{
		this(new BaseGovernment(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseGovernmentProxy.getOwner();
	}

	@Override
	public String getFleetName()
	{
		return baseGovernmentProxy.getFleetName();
	}

	@Override
	public String getPlanetName()
	{
		return baseGovernmentProxy.getPlanetName();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseGovernmentProxy.getNode();
	}

}
