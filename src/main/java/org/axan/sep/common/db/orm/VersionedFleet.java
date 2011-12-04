package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedFleet;
import org.axan.sep.common.db.orm.base.BaseVersionedFleet;
import org.axan.sep.common.db.IVersionedFleet;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedFleet extends VersionedUnit implements IVersionedFleet
{
	private final IBaseVersionedFleet baseVersionedFleetProxy;

	VersionedFleet(IBaseVersionedFleet baseVersionedFleetProxy, IGameConfig config)
	{
		super(baseVersionedFleetProxy, config);
		this.baseVersionedFleetProxy = baseVersionedFleetProxy;
	}

	public VersionedFleet(String owner, String name, eUnitType type, Integer turn, Location departure, Double progress, Location destination, IGameConfig config)
	{
		this(new BaseVersionedFleet(owner, name, type.toString(), turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z), config);
	}

	public VersionedFleet(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseVersionedFleet(stmnt), config);
	}

}
