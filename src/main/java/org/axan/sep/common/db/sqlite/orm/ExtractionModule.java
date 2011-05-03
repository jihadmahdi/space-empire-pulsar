package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Building;
import org.axan.sep.common.db.sqlite.orm.base.BaseExtractionModule;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class ExtractionModule extends Building implements IExtractionModule
{
	private final BaseExtractionModule baseExtractionModuleProxy;

	public ExtractionModule(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseExtractionModuleProxy = new BaseExtractionModule(stmnt);
	}

}
