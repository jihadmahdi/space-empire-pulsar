package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.base.IBaseUnit;
import org.axan.sep.common.db.orm.base.BaseUnit;
import org.axan.sep.common.db.IUnit;
import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

abstract class Unit implements IUnit
{
	private final IBaseUnit baseUnitProxy;

	Unit(IBaseUnit baseUnitProxy, org.axan.sep.common.db.IGameConfig config)
	{
		this.baseUnitProxy = baseUnitProxy;
	}

	@Override
	public String getName()
	{
		return baseUnitProxy.getName();
	}

	@Override
	public double getProgress()
	{
		return baseUnitProxy.getProgress();
	}	

	public static void initializeNode(Node node, String name, double progress)
	{
		node.setProperty("name", name);
		node.setProperty("progress", progress);
	}

}
