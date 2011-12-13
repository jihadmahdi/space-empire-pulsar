package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseCarbonOrder;
import org.axan.sep.common.db.orm.base.BaseCarbonOrder;
import org.axan.sep.common.db.ICarbonOrder;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class CarbonOrder implements ICarbonOrder
{
	private final IBaseCarbonOrder baseCarbonOrderProxy;

	CarbonOrder(IBaseCarbonOrder baseCarbonOrderProxy)
	{
		this.baseCarbonOrderProxy = baseCarbonOrderProxy;
	}

	public CarbonOrder(String owner, String source, Integer priority, Integer amount, String destination)
	{
		this(new BaseCarbonOrder(owner, source, priority, amount, destination));
	}

	public CarbonOrder(Node stmnt) throws Exception
	{
		this(new BaseCarbonOrder(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseCarbonOrderProxy.getOwner();
	}

	@Override
	public String getSource()
	{
		return baseCarbonOrderProxy.getSource();
	}

	@Override
	public Integer getPriority()
	{
		return baseCarbonOrderProxy.getPriority();
	}

	@Override
	public Integer getAmount()
	{
		return baseCarbonOrderProxy.getAmount();
	}

	@Override
	public String getDestination()
	{
		return baseCarbonOrderProxy.getDestination();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseCarbonOrderProxy.getNode();
	}

}
