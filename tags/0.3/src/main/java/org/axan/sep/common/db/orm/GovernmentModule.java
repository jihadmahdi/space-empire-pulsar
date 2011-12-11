package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGovernmentModule;
import org.axan.sep.common.db.orm.base.BaseGovernmentModule;
import org.axan.sep.common.db.orm.base.IBaseGovernmentModule;

public class GovernmentModule extends Building implements IGovernmentModule
{
	private final IBaseGovernmentModule baseGovernmentModuleProxy;

	GovernmentModule(IBaseGovernmentModule baseGovernmentModuleProxy)
	{
		super(baseGovernmentModuleProxy);
		this.baseGovernmentModuleProxy = baseGovernmentModuleProxy;
	}

	public GovernmentModule(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		this(new BaseGovernmentModule(type.toString(), celestialBodyName, turn, nbSlots));
	}

	public GovernmentModule(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseGovernmentModule(stmnt));
	}

}
