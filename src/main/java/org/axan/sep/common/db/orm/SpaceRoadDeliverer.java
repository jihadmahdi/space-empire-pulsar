package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Unit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseSpaceRoadDeliverer;
import org.axan.sep.common.db.orm.base.BaseSpaceRoadDeliverer;
import org.axan.sep.common.db.ISpaceRoadDeliverer;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;

public class SpaceRoadDeliverer extends Unit implements ISpaceRoadDeliverer
{
	private final IBaseSpaceRoadDeliverer baseSpaceRoadDelivererProxy;

	SpaceRoadDeliverer(IBaseSpaceRoadDeliverer baseSpaceRoadDelivererProxy, IGameConfig config)
	{
		super(baseSpaceRoadDelivererProxy, config);
		this.baseSpaceRoadDelivererProxy = baseSpaceRoadDelivererProxy;
	}

	public SpaceRoadDeliverer(String owner, String name, eUnitType type, String sourceType, String sourceCelestialBodyName, Integer sourceTurn, String destinationType, String destinationCelestialBodyName, Integer destinationTurn, IGameConfig config)
	{
		this(new BaseSpaceRoadDeliverer(owner, name, type.toString(), sourceType, sourceCelestialBodyName, sourceTurn, destinationType, destinationCelestialBodyName, destinationTurn), config);
	}

	public SpaceRoadDeliverer(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseSpaceRoadDeliverer(stmnt), config);
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
