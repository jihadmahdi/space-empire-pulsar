package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.Building;
import org.axan.sep.server.model.orm.base.BaseStarshipPlant;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class StarshipPlant extends Building implements IStarshipPlant
{
	private final BaseStarshipPlant baseStarshipPlantProxy;

	public StarshipPlant(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseStarshipPlantProxy = new BaseStarshipPlant(stmnt);
	}

}
