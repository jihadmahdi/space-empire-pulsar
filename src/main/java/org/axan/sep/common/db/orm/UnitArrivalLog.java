package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseUnitArrivalLog;
import org.axan.sep.common.db.orm.base.BaseUnitArrivalLog;
import org.axan.sep.common.db.IUnitArrivalLog;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class UnitArrivalLog implements IUnitArrivalLog
{
	private final IBaseUnitArrivalLog baseUnitArrivalLogProxy;

	UnitArrivalLog(IBaseUnitArrivalLog baseUnitArrivalLogProxy)
	{
		this.baseUnitArrivalLogProxy = baseUnitArrivalLogProxy;
	}

	public UnitArrivalLog(String owner, String unitName, String unitType, Integer logTurn, Integer instantTime, String destination, String vortex)
	{
		this(new BaseUnitArrivalLog(owner, unitName, unitType, logTurn, instantTime, destination, vortex));
	}

	public UnitArrivalLog(Node stmnt) throws Exception
	{
		this(new BaseUnitArrivalLog(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseUnitArrivalLogProxy.getOwner();
	}

	@Override
	public String getUnitName()
	{
		return baseUnitArrivalLogProxy.getUnitName();
	}

	@Override
	public String getUnitType()
	{
		return baseUnitArrivalLogProxy.getUnitType();
	}

	@Override
	public Integer getLogTurn()
	{
		return baseUnitArrivalLogProxy.getLogTurn();
	}

	@Override
	public Integer getInstantTime()
	{
		return baseUnitArrivalLogProxy.getInstantTime();
	}

	@Override
	public String getDestination()
	{
		return baseUnitArrivalLogProxy.getDestination();
	}

	@Override
	public String getVortex()
	{
		return baseUnitArrivalLogProxy.getVortex();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseUnitArrivalLogProxy.getNode();
	}

}
