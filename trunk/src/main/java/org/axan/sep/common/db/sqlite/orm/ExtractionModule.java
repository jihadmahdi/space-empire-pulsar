package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Building;
import org.axan.sep.common.db.sqlite.orm.base.BaseExtractionModule;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eBuildingType;

public class ExtractionModule extends Building implements IExtractionModule
{
	private final BaseExtractionModule baseExtractionModuleProxy;

	public ExtractionModule(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		super(type, celestialBodyName, turn, nbSlots);
		baseExtractionModuleProxy = new BaseExtractionModule(type.toString(), celestialBodyName, turn, nbSlots);
	}

	public ExtractionModule(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseExtractionModuleProxy = new BaseExtractionModule(stmnt);
	}

}
