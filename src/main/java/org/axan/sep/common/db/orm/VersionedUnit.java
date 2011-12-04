package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Unit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedUnit;
import org.axan.sep.common.db.orm.base.BaseVersionedUnit;
import org.axan.sep.common.db.IVersionedUnit;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedUnit extends Unit implements IVersionedUnit
{
	private final IBaseVersionedUnit baseVersionedUnitProxy;
	private final Location departure;
	private final Location destination;

	VersionedUnit(IBaseVersionedUnit baseVersionedUnitProxy, IGameConfig config)
	{
		super(baseVersionedUnitProxy, config);
		this.baseVersionedUnitProxy = baseVersionedUnitProxy;
		this.departure = (baseVersionedUnitProxy.getDeparture_x() == null ? null : new Location(baseVersionedUnitProxy.getDeparture_x(), baseVersionedUnitProxy.getDeparture_y(), baseVersionedUnitProxy.getDeparture_z()));
		this.destination = (baseVersionedUnitProxy.getDestination_x() == null ? null : new Location(baseVersionedUnitProxy.getDestination_x(), baseVersionedUnitProxy.getDestination_y(), baseVersionedUnitProxy.getDestination_z()));
	}

	public VersionedUnit(String owner, String name, eUnitType type, Integer turn, Location departure, Double progress, Location destination, IGameConfig config)
	{
		this(new BaseVersionedUnit(owner, name, type.toString(), turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z), config);
	}

	public VersionedUnit(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseVersionedUnit(stmnt), config);
	}

	public Integer getTurn()
	{
		return baseVersionedUnitProxy.getTurn();
	}

	public Location getDeparture()
	{
		return departure;
	}

	public Double getProgress()
	{
		return baseVersionedUnitProxy.getProgress();
	}

	public Location getDestination()
	{
		return destination;
	}

}
