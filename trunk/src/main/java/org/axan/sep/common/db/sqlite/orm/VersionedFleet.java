package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.VersionedUnit;
import org.axan.sep.common.db.sqlite.orm.Fleet;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedFleet;
import org.axan.sep.common.db.IVersionedFleet;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedFleet implements IVersionedFleet
{
	private final VersionedUnit versionedUnitProxy;
	private final Fleet fleetProxy;
	private final BaseVersionedFleet baseVersionedFleetProxy;

	public VersionedFleet(String owner, String name, eUnitType type, Integer turn, Location departure, Double progress, Location destination, float sight)
	{
		fleetProxy = new Fleet(owner, name, type, sight);
		versionedUnitProxy = new VersionedUnit(owner, name, type, turn, departure, progress, destination, sight);
		baseVersionedFleetProxy = new BaseVersionedFleet(owner, name, type.toString(), turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z);
	}

	public VersionedFleet(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.versionedUnitProxy = new VersionedUnit(stmnt, config);
		this.fleetProxy = new Fleet(stmnt, config);
		this.baseVersionedFleetProxy = new BaseVersionedFleet(stmnt);
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

}
