package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IPulsarMissile;
import org.axan.sep.common.db.orm.base.BasePulsarMissile;
import org.axan.sep.common.db.orm.base.IBasePulsarMissile;

public class PulsarMissile extends Unit implements IPulsarMissile
{
	private final IBasePulsarMissile basePulsarMissileProxy;
	private final Location direction;

	PulsarMissile(IBasePulsarMissile basePulsarMissileProxy, IGameConfig config)
	{
		super(basePulsarMissileProxy, config);
		this.basePulsarMissileProxy = basePulsarMissileProxy;
		this.direction = (basePulsarMissileProxy.getDirection_x() == null ? null : new Location(basePulsarMissileProxy.getDirection_x(), basePulsarMissileProxy.getDirection_y(), basePulsarMissileProxy.getDirection_z()));
	}

	public PulsarMissile(String owner, String name, eUnitType type, Location departure, Double progress, Location destination, Integer time, Integer volume, Location direction, IGameConfig config)
	{
		this(new BasePulsarMissile(owner, name, type.toString(), departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, time, volume, direction == null ? null : direction.x, direction == null ? null : direction.y, direction == null ? null : direction.z), config);
	}

	public PulsarMissile(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BasePulsarMissile(stmnt), config);
	}

	@Override
	public Integer getTime()
	{
		return basePulsarMissileProxy.getTime();
	}

	@Override
	public Integer getVolume()
	{
		return basePulsarMissileProxy.getVolume();
	}

	@Override
	public Location getDirection()
	{
		return direction;
	}

}
