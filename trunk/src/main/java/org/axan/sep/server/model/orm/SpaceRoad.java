package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseSpaceRoad;

public class SpaceRoad implements ISpaceRoad
{
	private final BaseSpaceRoad baseSpaceRoadProxy;

	public SpaceRoad(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseSpaceRoadProxy = new BaseSpaceRoad(stmnt);
	}

	public String getSpaceCounterBCelestialBodyName()
	{
		return baseSpaceRoadProxy.getSpaceCounterBCelestialBodyName();
	}

	public Integer getSpaceCounterATurn()
	{
		return baseSpaceRoadProxy.getSpaceCounterATurn();
	}

	public String getSpaceCounterAType()
	{
		return baseSpaceRoadProxy.getSpaceCounterAType();
	}

	public String getSpaceCounterBType()
	{
		return baseSpaceRoadProxy.getSpaceCounterBType();
	}

	public Integer getSpaceCounterBTurn()
	{
		return baseSpaceRoadProxy.getSpaceCounterBTurn();
	}

	public String getSpaceCounterACelestialBodyName()
	{
		return baseSpaceRoadProxy.getSpaceCounterACelestialBodyName();
	}

}
