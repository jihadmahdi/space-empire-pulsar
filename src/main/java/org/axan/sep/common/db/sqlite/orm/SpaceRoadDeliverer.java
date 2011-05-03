package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Unit;
import org.axan.sep.common.db.sqlite.orm.base.BaseSpaceRoadDeliverer;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

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
