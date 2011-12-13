package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseSpaceRoad;
import org.axan.sep.common.db.orm.base.BaseSpaceRoad;
import org.axan.sep.common.db.ISpaceRoad;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class SpaceRoad implements ISpaceRoad
{
	private final IBaseSpaceRoad baseSpaceRoadProxy;

	SpaceRoad(IBaseSpaceRoad baseSpaceRoadProxy)
	{
		this.baseSpaceRoadProxy = baseSpaceRoadProxy;
	}

	public SpaceRoad(String name, String builder, String spaceCounterAType, String spaceCounterACelestialBodyName, Integer spaceCounterATurn, String spaceCounterBType, String spaceCounterBCelestialBodyName, Integer spaceCounterBTurn)
	{
		this(new BaseSpaceRoad(name, builder, spaceCounterAType, spaceCounterACelestialBodyName, spaceCounterATurn, spaceCounterBType, spaceCounterBCelestialBodyName, spaceCounterBTurn));
	}

	public SpaceRoad(Node stmnt) throws Exception
	{
		this(new BaseSpaceRoad(stmnt));
	}

	@Override
	public String getName()
	{
		return baseSpaceRoadProxy.getName();
	}

	@Override
	public String getBuilder()
	{
		return baseSpaceRoadProxy.getBuilder();
	}

	@Override
	public String getSpaceCounterAType()
	{
		return baseSpaceRoadProxy.getSpaceCounterAType();
	}

	@Override
	public String getSpaceCounterACelestialBodyName()
	{
		return baseSpaceRoadProxy.getSpaceCounterACelestialBodyName();
	}

	@Override
	public Integer getSpaceCounterATurn()
	{
		return baseSpaceRoadProxy.getSpaceCounterATurn();
	}

	@Override
	public String getSpaceCounterBType()
	{
		return baseSpaceRoadProxy.getSpaceCounterBType();
	}

	@Override
	public String getSpaceCounterBCelestialBodyName()
	{
		return baseSpaceRoadProxy.getSpaceCounterBCelestialBodyName();
	}

	@Override
	public Integer getSpaceCounterBTurn()
	{
		return baseSpaceRoadProxy.getSpaceCounterBTurn();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseSpaceRoadProxy.getNode();
	}

}
