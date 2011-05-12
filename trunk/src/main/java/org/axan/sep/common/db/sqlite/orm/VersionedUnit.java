package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Unit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedUnit;
import org.axan.sep.common.db.IVersionedUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedUnit extends Unit implements IVersionedUnit
{
	private final BaseVersionedUnit baseVersionedUnitProxy;
	private Location departure;private Location destination;

	public VersionedUnit(String owner, String name, eUnitType type, Integer turn, Location departure, Double progress, Location destination, float sight)
	{
		super(owner, name, type, sight);
		baseVersionedUnitProxy = new BaseVersionedUnit(owner, name, type.toString(), turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z);
		this.departure = departure;
		this.destination = destination;
	}

	public VersionedUnit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseVersionedUnitProxy = new BaseVersionedUnit(stmnt);
		this.departure = (baseVersionedUnitProxy.getDeparture_x() == null ? null : new Location(baseVersionedUnitProxy.getDeparture_x(), baseVersionedUnitProxy.getDeparture_y(), baseVersionedUnitProxy.getDeparture_z()));
		this.destination = (baseVersionedUnitProxy.getDestination_x() == null ? null : new Location(baseVersionedUnitProxy.getDestination_x(), baseVersionedUnitProxy.getDestination_y(), baseVersionedUnitProxy.getDestination_z()));
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
