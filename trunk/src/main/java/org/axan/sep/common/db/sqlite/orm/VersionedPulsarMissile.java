package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.PulsarMissile;
import org.axan.sep.common.db.sqlite.orm.VersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedPulsarMissile;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public class VersionedPulsarMissile implements IVersionedPulsarMissile
{
	private final PulsarMissile pulsarMissileProxy;
	private final VersionedUnit versionedUnitProxy;
	private final BaseVersionedPulsarMissile baseVersionedPulsarMissileProxy;
	private Location direction;

	public VersionedPulsarMissile(String owner, String name, eUnitType type, Integer time, Integer volume, Integer turn, Location departure, Double progress, Location destination, Location direction, float sight)
	{
		pulsarMissileProxy = new PulsarMissile(owner, name, type, time, volume, sight);
		versionedUnitProxy = new VersionedUnit(owner, name, type, turn, departure, progress, destination, sight);
		baseVersionedPulsarMissileProxy = new BaseVersionedPulsarMissile(owner, name, type.toString(), time, volume, turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, direction == null ? null : direction.x, direction == null ? null : direction.y, direction == null ? null : direction.z);
		this.direction = direction;
	}

	public VersionedPulsarMissile(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.pulsarMissileProxy = new PulsarMissile(stmnt, config);
		this.versionedUnitProxy = new VersionedUnit(stmnt, config);
		this.baseVersionedPulsarMissileProxy = new BaseVersionedPulsarMissile(stmnt);
		this.direction = (baseVersionedPulsarMissileProxy.getDirection_x() == null ? null : new Location(baseVersionedPulsarMissileProxy.getDirection_x(), baseVersionedPulsarMissileProxy.getDirection_y(), baseVersionedPulsarMissileProxy.getDirection_z()));
	}

	@Override
	public float getSight()
	{
		return versionedUnitProxy.getSight();
	}
	
	public Location getDirection()
	{
		return direction;
	}

	public Integer getTime()
	{
		return pulsarMissileProxy.getTime();
	}

	public Integer getVolume()
	{
		return pulsarMissileProxy.getVolume();
	}

	public String getOwner()
	{
		return pulsarMissileProxy.getOwner();
	}

	public String getName()
	{
		return pulsarMissileProxy.getName();
	}

	public eUnitType getType()
	{
		return pulsarMissileProxy.getType();
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

}
