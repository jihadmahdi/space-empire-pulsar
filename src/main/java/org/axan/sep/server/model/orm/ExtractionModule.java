package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.Building;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseExtractionModule;

public class ExtractionModule extends Building implements IExtractionModule
{
	private final BaseExtractionModule baseExtractionModuleProxy;

	public ExtractionModule(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseExtractionModuleProxy = new BaseExtractionModule(stmnt);
	}

}
