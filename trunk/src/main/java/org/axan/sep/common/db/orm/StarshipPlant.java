package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IStarshipPlant;
import org.axan.sep.common.db.orm.base.BaseStarshipPlant;
import org.axan.sep.common.db.orm.base.IBaseStarshipPlant;

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

	public StarshipPlant(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseStarshipPlant(stmnt));
	}

}
