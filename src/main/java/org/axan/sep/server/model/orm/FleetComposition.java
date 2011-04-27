package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseFleetComposition;

public class FleetComposition implements IFleetComposition
{
	private final BaseFleetComposition baseFleetCompositionProxy;

	public FleetComposition(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseFleetCompositionProxy = new BaseFleetComposition(stmnt);
	}

	public String getFleetName()
	{
		return baseFleetCompositionProxy.getFleetName();
	}

	public Integer getFleetTurn()
	{
		return baseFleetCompositionProxy.getFleetTurn();
	}

	public String getFleetOwner()
	{
		return baseFleetCompositionProxy.getFleetOwner();
	}

	public Integer getQuantity()
	{
		return baseFleetCompositionProxy.getQuantity();
	}

	public String getStarshipTemplate()
	{
		return baseFleetCompositionProxy.getStarshipTemplate();
	}

}
