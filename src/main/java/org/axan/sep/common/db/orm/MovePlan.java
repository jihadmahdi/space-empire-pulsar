package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseMovePlan;
import org.axan.sep.common.db.orm.base.BaseMovePlan;
import org.axan.sep.common.db.IMovePlan;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class MovePlan implements IMovePlan
{
	private final IBaseMovePlan baseMovePlanProxy;

	MovePlan(IBaseMovePlan baseMovePlanProxy)
	{
		this.baseMovePlanProxy = baseMovePlanProxy;
	}

	public MovePlan(String owner, String name, Integer priority, Integer delay, Boolean attack, String destination)
	{
		this(new BaseMovePlan(owner, name, priority, delay, attack, destination));
	}

	public MovePlan(Node stmnt) throws Exception
	{
		this(new BaseMovePlan(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseMovePlanProxy.getOwner();
	}

	@Override
	public String getName()
	{
		return baseMovePlanProxy.getName();
	}

	@Override
	public Integer getPriority()
	{
		return baseMovePlanProxy.getPriority();
	}

	@Override
	public Integer getDelay()
	{
		return baseMovePlanProxy.getDelay();
	}

	@Override
	public Boolean getAttack()
	{
		return baseMovePlanProxy.getAttack();
	}

	@Override
	public String getDestination()
	{
		return baseMovePlanProxy.getDestination();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseMovePlanProxy.getNode();
	}

}
