package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Building;
import org.axan.sep.common.db.sqlite.orm.base.BaseGovernmentModule;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eBuildingType;

public class GovernmentModule extends Building implements IGovernmentModule
{
	private final BaseGovernmentModule baseGovernmentModuleProxy;

	public GovernmentModule(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		super(type, celestialBodyName, turn, nbSlots);
		baseGovernmentModuleProxy = new BaseGovernmentModule(type.toString(), celestialBodyName, turn, nbSlots);
	}

	public GovernmentModule(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseGovernmentModuleProxy = new BaseGovernmentModule(stmnt);
	}

}
