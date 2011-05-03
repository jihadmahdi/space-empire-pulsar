package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Building;
import org.axan.sep.common.db.sqlite.orm.base.BaseDefenseModule;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class DefenseModule extends Building implements IDefenseModule
{
	private final BaseDefenseModule baseDefenseModuleProxy;

	public DefenseModule(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseDefenseModuleProxy = new BaseDefenseModule(stmnt);
	}

}
