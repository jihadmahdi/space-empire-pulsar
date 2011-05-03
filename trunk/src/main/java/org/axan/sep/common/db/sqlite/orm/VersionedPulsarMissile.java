package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.PulsarMissile;
import org.axan.sep.common.db.sqlite.orm.VersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedPulsarMissile;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

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
	
	@Override
	public float getSight()
	{
		return versionedUnitProxy.getSight();
	}

	public Location getDirection()
	{
		return (baseVersionedPulsarMissileProxy.getDirection_x() == null) ? null : new Location(baseVersionedPulsarMissileProxy.getDirection_x(), baseVersionedPulsarMissileProxy.getDirection_y(), baseVersionedPulsarMissileProxy.getDirection_z());
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

	public String getName()
	{
		return pulsarMissileProxy.getName();
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

}
