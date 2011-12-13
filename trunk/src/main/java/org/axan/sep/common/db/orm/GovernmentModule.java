package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Building;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseGovernmentModule;
import org.axan.sep.common.db.orm.base.BaseGovernmentModule;
import org.axan.sep.common.db.IGovernmentModule;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class GovernmentModule extends Building implements IGovernmentModule
{
	private final IBaseGovernmentModule baseGovernmentModuleProxy;

	GovernmentModule(IBaseGovernmentModule baseGovernmentModuleProxy)
	{
		super(baseGovernmentModuleProxy);
		this.baseGovernmentModuleProxy = baseGovernmentModuleProxy;
	}

	public GovernmentModule(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		this(new BaseGovernmentModule(type.toString(), celestialBodyName, turn, nbSlots));
	}

	public GovernmentModule(Node stmnt) throws Exception
	{
		this(new BaseGovernmentModule(stmnt));
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseGovernmentModuleProxy.getNode();
	}

}
