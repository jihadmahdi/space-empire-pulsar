package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.VersionedUnit;
import org.axan.sep.common.db.sqlite.orm.SpaceRoadDeliverer;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedSpaceRoadDeliverer;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public class VersionedSpaceRoadDeliverer implements IVersionedSpaceRoadDeliverer
{
	private final VersionedUnit versionedUnitProxy;
	private final SpaceRoadDeliverer spaceRoadDelivererProxy;
	private final BaseVersionedSpaceRoadDeliverer baseVersionedSpaceRoadDelivererProxy;

	public VersionedSpaceRoadDeliverer(String owner, String name, eUnitType type, Integer turn, Location departure, Double progress, Location destination, String sourceType, String sourceCelestialBodyName, Integer sourceTurn, String destinationType, String destinationCelestialBodyName, Integer destinationTurn, float sight)
	{
		spaceRoadDelivererProxy = new SpaceRoadDeliverer(owner, name, type, sourceType, sourceCelestialBodyName, sourceTurn, destinationType, destinationCelestialBodyName, destinationTurn, sight);
		versionedUnitProxy = new VersionedUnit(owner, name, type, turn, departure, progress, destination, sight);
		baseVersionedSpaceRoadDelivererProxy = new BaseVersionedSpaceRoadDeliverer(owner, name, type.toString(), turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, sourceType, sourceCelestialBodyName, sourceTurn, destinationType, destinationCelestialBodyName, destinationTurn);
	}

	public VersionedSpaceRoadDeliverer(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.versionedUnitProxy = new VersionedUnit(stmnt, config);
		this.spaceRoadDelivererProxy = new SpaceRoadDeliverer(stmnt, config);
		this.baseVersionedSpaceRoadDelivererProxy = new BaseVersionedSpaceRoadDeliverer(stmnt);
	}
	
	@Override
	public float getSight()
	{
		return versionedUnitProxy.getSight();
	}

	public Integer getTurn()
	{
		return versionedUnitProxy.getTurn();
	}

	public Location getDeparture()
	{
		return versionedUnitProxy.getDeparture();
	}

	public Double getProgress()
	{
		return versionedUnitProxy.getProgress();
	}

	public Location getDestination()
	{
		return versionedUnitProxy.getDestination();
	}

	public String getOwner()
	{
		return versionedUnitProxy.getOwner();
	}

	public String getName()
	{
		return versionedUnitProxy.getName();
	}

	public eUnitType getType()
	{
		return versionedUnitProxy.getType();
	}

	public String getSourceType()
	{
		return spaceRoadDelivererProxy.getSourceType();
	}

	public String getSourceCelestialBodyName()
	{
		return spaceRoadDelivererProxy.getSourceCelestialBodyName();
	}

	public Integer getSourceTurn()
	{
		return spaceRoadDelivererProxy.getSourceTurn();
	}

	public String getDestinationType()
	{
		return spaceRoadDelivererProxy.getDestinationType();
	}

	public String getDestinationCelestialBodyName()
	{
		return spaceRoadDelivererProxy.getDestinationCelestialBodyName();
	}

	public Integer getDestinationTurn()
	{
		return spaceRoadDelivererProxy.getDestinationTurn();
	}

}
