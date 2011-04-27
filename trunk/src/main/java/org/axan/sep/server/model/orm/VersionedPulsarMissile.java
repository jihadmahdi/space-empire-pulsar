package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.model.orm.PulsarMissile;
import org.axan.sep.server.model.orm.VersionedUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseVersionedPulsarMissile;

public class VersionedPulsarMissile implements IVersionedPulsarMissile
{
	private final PulsarMissile pulsarMissileProxy;
	private final VersionedUnit versionedUnitProxy;
	private final BaseVersionedPulsarMissile baseVersionedPulsarMissileProxy;

	public VersionedPulsarMissile(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.pulsarMissileProxy = new PulsarMissile(stmnt, config);
		this.versionedUnitProxy = new VersionedUnit(stmnt, config);
		this.baseVersionedPulsarMissileProxy = new BaseVersionedPulsarMissile(stmnt);
	}

	public Integer getDirection_z()
	{
		return baseVersionedPulsarMissileProxy.getDirection_z();
	}

	public Integer getDirection_y()
	{
		return baseVersionedPulsarMissileProxy.getDirection_y();
	}

	public Integer getDirection_x()
	{
		return baseVersionedPulsarMissileProxy.getDirection_x();
	}

	public Integer getVolume()
	{
		return pulsarMissileProxy.getVolume();
	}

	public Integer getTime()
	{
		return pulsarMissileProxy.getTime();
	}

	public String getOwner()
	{
		return pulsarMissileProxy.getOwner();
	}

	public eUnitType getType()
	{
		return pulsarMissileProxy.getType();
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
		return pulsarMissileProxy.getName();
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

}
