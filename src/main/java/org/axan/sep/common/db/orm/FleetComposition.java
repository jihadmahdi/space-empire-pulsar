package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseFleetComposition;
import org.axan.sep.common.db.orm.base.BaseFleetComposition;
import org.axan.sep.common.db.IFleetComposition;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class FleetComposition implements IFleetComposition
{
	private final IBaseFleetComposition baseFleetCompositionProxy;

	FleetComposition(IBaseFleetComposition baseFleetCompositionProxy)
	{
		this.baseFleetCompositionProxy = baseFleetCompositionProxy;
	}

	public FleetComposition(String fleetOwner, String fleetName, String starshipTemplate, Integer quantity)
	{
		this(new BaseFleetComposition(fleetOwner, fleetName, starshipTemplate, quantity));
	}

	public FleetComposition(Node stmnt) throws Exception
	{
		this(new BaseFleetComposition(stmnt));
	}

	@Override
	public String getFleetOwner()
	{
		return baseFleetCompositionProxy.getFleetOwner();
	}

	@Override
	public String getFleetName()
	{
		return baseFleetCompositionProxy.getFleetName();
	}

	@Override
	public String getStarshipTemplate()
	{
		return baseFleetCompositionProxy.getStarshipTemplate();
	}

	@Override
	public Integer getQuantity()
	{
		return baseFleetCompositionProxy.getQuantity();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseFleetCompositionProxy.getNode();
	}

}
