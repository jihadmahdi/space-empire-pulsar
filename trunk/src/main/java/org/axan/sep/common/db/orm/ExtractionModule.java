package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Building;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseExtractionModule;
import org.axan.sep.common.db.orm.base.BaseExtractionModule;
import org.axan.sep.common.db.IExtractionModule;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

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

	public ExtractionModule(Node stmnt) throws Exception
	{
		this(new BaseExtractionModule(stmnt));
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseExtractionModuleProxy.getNode();
	}

}
