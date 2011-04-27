package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.Building;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseDefenseModule;

public class DefenseModule extends Building implements IDefenseModule
{
	private final BaseDefenseModule baseDefenseModuleProxy;

	public DefenseModule(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseDefenseModuleProxy = new BaseDefenseModule(stmnt);
	}

}
