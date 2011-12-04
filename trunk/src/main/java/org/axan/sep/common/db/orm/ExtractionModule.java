package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Building;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseExtractionModule;
import org.axan.sep.common.db.orm.base.BaseExtractionModule;
import org.axan.sep.common.db.IExtractionModule;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;

public class ExtractionModule extends Building implements IExtractionModule
{
	private final IBaseExtractionModule baseExtractionModuleProxy;

	ExtractionModule(IBaseExtractionModule baseExtractionModuleProxy)
	{
		super(baseExtractionModuleProxy);
		this.baseExtractionModuleProxy = baseExtractionModuleProxy;
	}

	public ExtractionModule(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		this(new BaseExtractionModule(type.toString(), celestialBodyName, turn, nbSlots));
	}

	public ExtractionModule(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseExtractionModule(stmnt));
	}

}
