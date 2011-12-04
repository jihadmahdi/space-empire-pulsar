package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedSpaceRoadDeliverer;
import org.axan.sep.common.db.orm.base.BaseVersionedSpaceRoadDeliverer;
import org.axan.sep.common.db.IVersionedSpaceRoadDeliverer;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedSpaceRoadDeliverer extends VersionedUnit implements IVersionedSpaceRoadDeliverer
{
	private final IBaseVersionedSpaceRoadDeliverer baseVersionedSpaceRoadDelivererProxy;

	VersionedSpaceRoadDeliverer(IBaseVersionedSpaceRoadDeliverer baseVersionedSpaceRoadDelivererProxy, IGameConfig config)
	{
		super(baseVersionedSpaceRoadDelivererProxy, config);
		this.baseVersionedSpaceRoadDelivererProxy = baseVersionedSpaceRoadDelivererProxy;
	}

	public VersionedSpaceRoadDeliverer(String owner, String name, eUnitType type, Integer turn, Location departure, Double progress, Location destination, String sourceType, String sourceCelestialBodyName, Integer sourceTurn, String destinationType, String destinationCelestialBodyName, Integer destinationTurn, IGameConfig config)
	{
		this(new BaseVersionedSpaceRoadDeliverer(owner, name, type.toString(), turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, sourceType, sourceCelestialBodyName, sourceTurn, destinationType, destinationCelestialBodyName, destinationTurn), config);
	}

	public VersionedSpaceRoadDeliverer(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseVersionedSpaceRoadDeliverer(stmnt), config);
	}

	public String getSourceType()
	{
		return baseVersionedSpaceRoadDelivererProxy.getSourceType();
	}

	public String getSourceCelestialBodyName()
	{
		return baseVersionedSpaceRoadDelivererProxy.getSourceCelestialBodyName();
	}

	public Integer getSourceTurn()
	{
		return baseVersionedSpaceRoadDelivererProxy.getSourceTurn();
	}

	public String getDestinationType()
	{
		return baseVersionedSpaceRoadDelivererProxy.getDestinationType();
	}

	public String getDestinationCelestialBodyName()
	{
		return baseVersionedSpaceRoadDelivererProxy.getDestinationCelestialBodyName();
	}

	public Integer getDestinationTurn()
	{
		return baseVersionedSpaceRoadDelivererProxy.getDestinationTurn();
	}

}
