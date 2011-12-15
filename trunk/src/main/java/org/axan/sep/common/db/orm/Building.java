package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.base.IBaseBuilding;
import org.axan.sep.common.db.orm.base.BaseBuilding;
import org.axan.sep.common.db.IBuilding;
import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public class Building implements IBuilding
{
	private final IBaseBuilding baseBuildingProxy;

	Building(IBaseBuilding baseBuildingProxy)
	{
		this.baseBuildingProxy = baseBuildingProxy;
	}

	public Building(int builtDate, int nbSlots)
	{
		this(new BaseBuilding(builtDate, nbSlots));
	}

	public Building(Node stmnt)
	{
		this(new BaseBuilding(stmnt));
	}

	@Override
	public int getBuiltDate()
	{
		return baseBuildingProxy.getBuiltDate();
	}

	@Override
	public int getNbSlots()
	{
		return baseBuildingProxy.getNbSlots();
	}

}
