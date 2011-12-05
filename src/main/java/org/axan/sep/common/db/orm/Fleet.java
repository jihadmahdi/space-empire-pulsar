package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IFleet;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.orm.base.BaseFleet;
import org.axan.sep.common.db.orm.base.IBaseFleet;

public class Fleet extends Unit implements IFleet
{
	private final IBaseFleet baseFleetProxy;

	Fleet(IBaseFleet baseFleetProxy, IGameConfig config)
	{
		super(baseFleetProxy, config);
		this.baseFleetProxy = baseFleetProxy;
	}

	public Fleet(String owner, String name, eUnitType type, Location departure, Double progress, Location destination, IGameConfig config)
	{
		this(new BaseFleet(owner, name, type.toString(), departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z), config);
	}

	public Fleet(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseFleet(stmnt), config);
	}

}
