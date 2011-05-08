package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Building;
import org.axan.sep.common.db.sqlite.orm.base.BaseDefenseModule;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eBuildingType;

public class DefenseModule extends Building implements IDefenseModule
{
	private final BaseDefenseModule baseDefenseModuleProxy;

	public DefenseModule(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		super(type, celestialBodyName, turn, nbSlots);
		baseDefenseModuleProxy = new BaseDefenseModule(type.toString(), celestialBodyName, turn, nbSlots);
	}

	public DefenseModule(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseDefenseModuleProxy = new BaseDefenseModule(stmnt);
	}

}
