package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.VersionedUnit;
import org.axan.sep.server.model.orm.Fleet;
import org.axan.sep.server.model.orm.base.BaseVersionedFleet;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eUnitType;
import com.almworks.sqlite4java.SQLiteStatement;

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
