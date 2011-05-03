package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ProductiveCelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseNebula;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class Nebula extends ProductiveCelestialBody implements INebula
{
	private final BaseNebula baseNebulaProxy;

	public Nebula(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseNebulaProxy = new BaseNebula(stmnt);
	}

}
