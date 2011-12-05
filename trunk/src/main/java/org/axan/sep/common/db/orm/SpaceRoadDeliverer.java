package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.ISpaceRoadDeliverer;
import org.axan.sep.common.db.orm.base.BaseSpaceRoadDeliverer;
import org.axan.sep.common.db.orm.base.IBaseSpaceRoadDeliverer;

public class SpaceRoadDeliverer extends Unit implements ISpaceRoadDeliverer
{
	private final IBaseSpaceRoadDeliverer baseSpaceRoadDelivererProxy;

	SpaceRoadDeliverer(IBaseSpaceRoadDeliverer baseSpaceRoadDelivererProxy, IGameConfig config)
	{
		super(baseSpaceRoadDelivererProxy, config);
		this.baseSpaceRoadDelivererProxy = baseSpaceRoadDelivererProxy;
	}

	public SpaceRoadDeliverer(String owner, String name, eUnitType type, Location departure, Double progress, Location destination, String sourceType, String sourceCelestialBodyName, Integer sourceTurn, String destinationType, String destinationCelestialBodyName, Integer destinationTurn, IGameConfig config)
	{
		this(new BaseSpaceRoadDeliverer(owner, name, type.toString(), departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, sourceType, sourceCelestialBodyName, sourceTurn, destinationType, destinationCelestialBodyName, destinationTurn), config);
	}

	public SpaceRoadDeliverer(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseSpaceRoadDeliverer(stmnt), config);
	}

	@Override
	public String getSourceType()
	{
		return baseSpaceRoadDelivererProxy.getSourceType();
	}

	@Override
	public String getSourceCelestialBodyName()
	{
		return baseSpaceRoadDelivererProxy.getSourceCelestialBodyName();
	}

	@Override
	public Integer getSourceTurn()
	{
		return baseSpaceRoadDelivererProxy.getSourceTurn();
	}

	@Override
	public String getDestinationType()
	{
		return baseSpaceRoadDelivererProxy.getDestinationType();
	}

	@Override
	public String getDestinationCelestialBodyName()
	{
		return baseSpaceRoadDelivererProxy.getDestinationCelestialBodyName();
	}

	@Override
	public Integer getDestinationTurn()
	{
		return baseSpaceRoadDelivererProxy.getDestinationTurn();
	}

}
