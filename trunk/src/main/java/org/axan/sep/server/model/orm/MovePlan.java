package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseMovePlan;

public class MovePlan implements IMovePlan
{
	private final BaseMovePlan baseMovePlanProxy;

	public MovePlan(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseMovePlanProxy = new BaseMovePlan(stmnt);
	}

	public Integer getPriority()
	{
		return baseMovePlanProxy.getPriority();
	}

	public String getOwner()
	{
		return baseMovePlanProxy.getOwner();
	}

	public Boolean getAttack()
	{
		return baseMovePlanProxy.getAttack();
	}

	public String getDestination()
	{
		return baseMovePlanProxy.getDestination();
	}

	public Integer getTurn()
	{
		return baseMovePlanProxy.getTurn();
	}

	public Integer getDelay()
	{
		return baseMovePlanProxy.getDelay();
	}

	public String getName()
	{
		return baseMovePlanProxy.getName();
	}

}
