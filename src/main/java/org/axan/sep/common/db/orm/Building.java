package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseBuilding;
import org.axan.sep.common.db.orm.base.BaseBuilding;
import org.axan.sep.common.db.IBuilding;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class Building implements IBuilding
{
	private final IBaseBuilding baseBuildingProxy;
	private final eBuildingType type;

	Building(IBaseBuilding baseBuildingProxy)
	{
		this.baseBuildingProxy = baseBuildingProxy;
		this.type = eBuildingType.valueOf(baseBuildingProxy.getType());
	}

	public Building(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		this(new BaseBuilding(type.toString(), celestialBodyName, turn, nbSlots));
	}

	public Building(Node stmnt) throws Exception
	{
		this(new BaseBuilding(stmnt));
	}

	@Override
	public eBuildingType getType()
	{
		return type;
	}

	@Override
	public String getCelestialBodyName()
	{
		return baseBuildingProxy.getCelestialBodyName();
	}

	@Override
	public Integer getTurn()
	{
		return baseBuildingProxy.getTurn();
	}

	@Override
	public Integer getNbSlots()
	{
		return baseBuildingProxy.getNbSlots();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseBuildingProxy.getNode();
	}

}
