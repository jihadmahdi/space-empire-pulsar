package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.Unit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseSpaceRoadDeliverer;

public class SpaceRoadDeliverer extends Unit implements ISpaceRoadDeliverer
{
	private final BaseSpaceRoadDeliverer baseSpaceRoadDelivererProxy;

	public SpaceRoadDeliverer(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseSpaceRoadDelivererProxy = new BaseSpaceRoadDeliverer(stmnt);
	}

	public String getDestinationType()
	{
		return baseSpaceRoadDelivererProxy.getDestinationType();
	}

	public Integer getDestinationTurn()
	{
		return baseSpaceRoadDelivererProxy.getDestinationTurn();
	}

	public String getSourceCelestialBodyName()
	{
		return baseSpaceRoadDelivererProxy.getSourceCelestialBodyName();
	}

	public Integer getSourceTurn()
	{
		return baseSpaceRoadDelivererProxy.getSourceTurn();
	}

	public String getSourceType()
	{
		return baseSpaceRoadDelivererProxy.getSourceType();
	}

	public String getDestinationCelestialBodyName()
	{
		return baseSpaceRoadDelivererProxy.getDestinationCelestialBodyName();
	}

}
