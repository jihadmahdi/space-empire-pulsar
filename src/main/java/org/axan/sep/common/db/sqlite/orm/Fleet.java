package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Unit;
import org.axan.sep.common.db.sqlite.orm.base.BaseFleet;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class Fleet extends Unit implements IFleet
{
	private final BaseFleet baseFleetProxy;

	public Fleet(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseFleetProxy = new BaseFleet(stmnt);
	}

}
