package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Unit;
import org.axan.sep.common.db.sqlite.orm.base.BaseFleet;
import org.axan.sep.common.db.IFleet;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;

public class Fleet extends Unit implements IFleet
{
	private final BaseFleet baseFleetProxy;

	public Fleet(String owner, String name, eUnitType type, float sight)
	{
		super(owner, name, type, sight);
		baseFleetProxy = new BaseFleet(owner, name, type.toString());
	}

	public Fleet(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseFleetProxy = new BaseFleet(stmnt);
	}

}
