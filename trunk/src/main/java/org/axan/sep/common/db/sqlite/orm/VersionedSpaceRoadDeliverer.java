package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.SpaceRoadDeliverer;
import org.axan.sep.common.db.sqlite.orm.VersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedSpaceRoadDeliverer;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public class VersionedSpaceRoadDeliverer implements IVersionedSpaceRoadDeliverer
{
	private final VersionedUnit versionedUnitProxy;
	private final SpaceRoadDeliverer spaceRoadDelivererProxy;
	private final BaseVersionedSpaceRoadDeliverer baseVersionedSpaceRoadDelivererProxy;

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

	public Integer getTurn()
	{
		return versionedUnitProxy.getTurn();
	}

	public String getOwner()
	{
		return versionedUnitProxy.getOwner();
	}

	public eUnitType getType()
	{
		return versionedUnitProxy.getType();
	}

	public String getName()
	{
		return versionedUnitProxy.getName();
	}

	public String getDestinationType()
	{
		return spaceRoadDelivererProxy.getDestinationType();
	}

	public Integer getDestinationTurn()
	{
		return spaceRoadDelivererProxy.getDestinationTurn();
	}

	public String getSourceCelestialBodyName()
	{
		return spaceRoadDelivererProxy.getSourceCelestialBodyName();
	}

	public Integer getSourceTurn()
	{
		return spaceRoadDelivererProxy.getSourceTurn();
	}

	public String getSourceType()
	{
		return spaceRoadDelivererProxy.getSourceType();
	}

	public String getDestinationCelestialBodyName()
	{
		return spaceRoadDelivererProxy.getDestinationCelestialBodyName();
	}

}
