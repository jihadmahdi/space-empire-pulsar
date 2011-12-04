package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Unit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseFleet;
import org.axan.sep.common.db.orm.base.BaseFleet;
import org.axan.sep.common.db.IFleet;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;

public class Fleet extends Unit implements IFleet
{
	private final IBaseFleet baseFleetProxy;

	Fleet(IBaseFleet baseFleetProxy, IGameConfig config)
	{
		super(baseFleetProxy, config);
		this.baseFleetProxy = baseFleetProxy;
	}

	public Fleet(String owner, String name, eUnitType type, IGameConfig config)
	{
		this(new BaseFleet(owner, name, type.toString()), config);
	}

	public Fleet(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseFleet(stmnt), config);
	}

}
