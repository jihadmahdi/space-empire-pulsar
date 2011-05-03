package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Unit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedUnit;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.SEPUtils.Location;

public class VersionedUnit extends Unit implements IVersionedUnit
{
	private final BaseVersionedUnit baseVersionedUnitProxy;

	public VersionedUnit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseVersionedUnitProxy = new BaseVersionedUnit(stmnt);
	}

	public Location getDeparture()
	{
		return (baseVersionedUnitProxy.getDeparture_x() == null) ? null : new Location(baseVersionedUnitProxy.getDeparture_x(), baseVersionedUnitProxy.getDeparture_y(), baseVersionedUnitProxy.getDeparture_z());
	}

	public Double getProgress()
	{
		return baseVersionedUnitProxy.getProgress();
	}

	public Location getDestination()
	{
		return (baseVersionedUnitProxy.getDestination_x() == null) ? null : new Location(baseVersionedUnitProxy.getDestination_x(), baseVersionedUnitProxy.getDestination_y(), baseVersionedUnitProxy.getDestination_z());
	}

	public Integer getTurn()
	{
		return baseVersionedUnitProxy.getTurn();
	}

}
