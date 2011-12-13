package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Building;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseSpaceCounter;
import org.axan.sep.common.db.orm.base.BaseSpaceCounter;
import org.axan.sep.common.db.ISpaceCounter;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class SpaceCounter extends Building implements ISpaceCounter
{
	private final IBaseSpaceCounter baseSpaceCounterProxy;

	SpaceCounter(IBaseSpaceCounter baseSpaceCounterProxy)
	{
		super(baseSpaceCounterProxy);
		this.baseSpaceCounterProxy = baseSpaceCounterProxy;
	}

	public SpaceCounter(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		this(new BaseSpaceCounter(type.toString(), celestialBodyName, turn, nbSlots));
	}

	public SpaceCounter(Node stmnt) throws Exception
	{
		this(new BaseSpaceCounter(stmnt));
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseSpaceCounterProxy.getNode();
	}

}
