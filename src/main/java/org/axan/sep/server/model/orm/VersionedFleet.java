package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.model.orm.VersionedUnit;
import org.axan.sep.server.model.orm.Fleet;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseVersionedFleet;

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

	public Double getProgress()
	{
		return versionedUnitProxy.getProgress();
	}

	@Override
	public RealLocation getDeparture()
	{
		return versionedUnitProxy.getDeparture();
	}
	
	@Override
	public RealLocation getDestination()
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
	
	@Override
	public float getSight()
	{
		return versionedUnitProxy.getSight();
	}

	@Override
	public float getSpeed()
	{
		return versionedUnitProxy.getSpeed();
	}

	public String getName()
	{
		return versionedUnitProxy.getName();
	}

}
