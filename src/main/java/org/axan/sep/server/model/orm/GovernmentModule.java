package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.Building;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseGovernmentModule;

public class GovernmentModule extends Building implements IGovernmentModule
{
	private final BaseGovernmentModule baseGovernmentModuleProxy;

	public GovernmentModule(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseGovernmentModuleProxy = new BaseGovernmentModule(stmnt);
	}

}
