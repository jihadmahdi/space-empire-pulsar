package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseCarbonOrder;

public class CarbonOrder implements ICarbonOrder
{
	private final BaseCarbonOrder baseCarbonOrderProxy;

	public CarbonOrder(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseCarbonOrderProxy = new BaseCarbonOrder(stmnt);
	}

	public String getSource()
	{
		return baseCarbonOrderProxy.getSource();
	}

	public Integer getAmount()
	{
		return baseCarbonOrderProxy.getAmount();
	}

	public Integer getPriority()
	{
		return baseCarbonOrderProxy.getPriority();
	}

	public String getOwner()
	{
		return baseCarbonOrderProxy.getOwner();
	}

	public String getDestination()
	{
		return baseCarbonOrderProxy.getDestination();
	}

}
