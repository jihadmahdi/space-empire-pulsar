package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Building;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseStarshipPlant;
import org.axan.sep.common.db.orm.base.BaseStarshipPlant;
import org.axan.sep.common.db.IStarshipPlant;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class StarshipPlant extends Building implements IStarshipPlant
{
	private final IBaseStarshipPlant baseStarshipPlantProxy;

	StarshipPlant(IBaseStarshipPlant baseStarshipPlantProxy)
	{
		super(baseStarshipPlantProxy);
		this.baseStarshipPlantProxy = baseStarshipPlantProxy;
	}

	public StarshipPlant(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		this(new BaseStarshipPlant(type.toString(), celestialBodyName, turn, nbSlots));
	}

	public StarshipPlant(Node stmnt) throws Exception
	{
		this(new BaseStarshipPlant(stmnt));
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseStarshipPlantProxy.getNode();
	}

}
