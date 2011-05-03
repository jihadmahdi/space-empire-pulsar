package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Fleet;
import org.axan.sep.common.db.sqlite.orm.VersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedFleet;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public class VersionedFleet implements IVersionedFleet
{
	private final VersionedUnit versionedUnitProxy;
	private final Fleet fleetProxy;
	private final BaseVersionedFleet baseVersionedFleetProxy;

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

}
