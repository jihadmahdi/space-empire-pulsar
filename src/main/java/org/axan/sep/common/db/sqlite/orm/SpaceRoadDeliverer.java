package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Unit;
import org.axan.sep.common.db.sqlite.orm.base.BaseSpaceRoadDeliverer;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;

public class SpaceRoadDeliverer extends Unit implements ISpaceRoadDeliverer
{
	private final BaseSpaceRoadDeliverer baseSpaceRoadDelivererProxy;

	public SpaceRoadDeliverer(String owner, String name, eUnitType type, String sourceType, String sourceCelestialBodyName, Integer sourceTurn, String destinationType, String destinationCelestialBodyName, Integer destinationTurn, float sight)
	{
		super(owner, name, type, sight);
		baseSpaceRoadDelivererProxy = new BaseSpaceRoadDeliverer(owner, name, type.toString(), sourceType, sourceCelestialBodyName, sourceTurn, destinationType, destinationCelestialBodyName, destinationTurn);
	}

	public SpaceRoadDeliverer(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseSpaceRoadDelivererProxy = new BaseSpaceRoadDeliverer(stmnt);
	}

	public String getSourceType()
	{
		return baseSpaceRoadDelivererProxy.getSourceType();
	}

	public String getSourceCelestialBodyName()
	{
		return baseSpaceRoadDelivererProxy.getSourceCelestialBodyName();
	}

	public Integer getSourceTurn()
	{
		return baseSpaceRoadDelivererProxy.getSourceTurn();
	}

	public String getDestinationType()
	{
		return baseSpaceRoadDelivererProxy.getDestinationType();
	}

	public String getDestinationCelestialBodyName()
	{
		return baseSpaceRoadDelivererProxy.getDestinationCelestialBodyName();
	}

	public Integer getDestinationTurn()
	{
		return baseSpaceRoadDelivererProxy.getDestinationTurn();
	}

}
