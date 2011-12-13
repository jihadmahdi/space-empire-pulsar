package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Building;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseDefenseModule;
import org.axan.sep.common.db.orm.base.BaseDefenseModule;
import org.axan.sep.common.db.IDefenseModule;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class DefenseModule extends Building implements IDefenseModule
{
	private final IBaseDefenseModule baseDefenseModuleProxy;

	DefenseModule(IBaseDefenseModule baseDefenseModuleProxy)
	{
		super(baseDefenseModuleProxy);
		this.baseDefenseModuleProxy = baseDefenseModuleProxy;
	}

	public DefenseModule(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		this(new BaseDefenseModule(type.toString(), celestialBodyName, turn, nbSlots));
	}

	public DefenseModule(Node stmnt) throws Exception
	{
		this(new BaseDefenseModule(stmnt));
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseDefenseModuleProxy.getNode();
	}

}
