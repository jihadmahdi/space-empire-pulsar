package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.ProductiveCelestialBody;
import org.axan.sep.server.model.orm.base.BaseNebula;
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
