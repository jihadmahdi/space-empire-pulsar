package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Building;
import org.axan.sep.common.db.sqlite.orm.base.BaseStarshipPlant;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eBuildingType;

public class StarshipPlant extends Building implements IStarshipPlant
{
	private final BaseStarshipPlant baseStarshipPlantProxy;

	public StarshipPlant(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		super(type, celestialBodyName, turn, nbSlots);
		baseStarshipPlantProxy = new BaseStarshipPlant(type.toString(), celestialBodyName, turn, nbSlots);
	}

	public StarshipPlant(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseStarshipPlantProxy = new BaseStarshipPlant(stmnt);
	}

}
