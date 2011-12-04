package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedPulsarMissile;
import org.axan.sep.common.db.orm.base.BaseVersionedPulsarMissile;
import org.axan.sep.common.db.IVersionedPulsarMissile;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedPulsarMissile extends VersionedUnit implements IVersionedPulsarMissile
{
	private final IBaseVersionedPulsarMissile baseVersionedPulsarMissileProxy;
	private final Location direction;

	VersionedPulsarMissile(IBaseVersionedPulsarMissile baseVersionedPulsarMissileProxy, IGameConfig config)
	{
		super(baseVersionedPulsarMissileProxy, config);
		this.baseVersionedPulsarMissileProxy = baseVersionedPulsarMissileProxy;
		this.direction = (baseVersionedPulsarMissileProxy.getDirection_x() == null ? null : new Location(baseVersionedPulsarMissileProxy.getDirection_x(), baseVersionedPulsarMissileProxy.getDirection_y(), baseVersionedPulsarMissileProxy.getDirection_z()));
	}

	public VersionedPulsarMissile(String owner, String name, eUnitType type, Integer time, Integer volume, Integer turn, Location departure, Double progress, Location destination, Location direction, IGameConfig config)
	{
		this(new BaseVersionedPulsarMissile(owner, name, type.toString(), time, volume, turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, direction == null ? null : direction.x, direction == null ? null : direction.y, direction == null ? null : direction.z), config);
	}

	public VersionedPulsarMissile(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseVersionedPulsarMissile(stmnt), config);
	}

	public Location getDirection()
	{
		return direction;
	}

	public Integer getTime()
	{
		return baseVersionedPulsarMissileProxy.getTime();
	}

	public Integer getVolume()
	{
		return baseVersionedPulsarMissileProxy.getVolume();
	}

}
