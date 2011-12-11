package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IDefenseModule;
import org.axan.sep.common.db.orm.base.BaseDefenseModule;
import org.axan.sep.common.db.orm.base.IBaseDefenseModule;

public class DefenseModule extends Building implements IDefenseModule
{
	private final IBaseDefenseModule baseDefenseModuleProxy;

	DefenseModule(IBaseDefenseModule baseDefenseModuleProxy)
	{
		super(baseDefenseModuleProxy);
		this.baseDefenseModuleProxy = baseDefenseModuleProxy;
	}

	public DefenseModule(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots)
	{
		this(new BaseDefenseModule(type.toString(), celestialBodyName, turn, nbSlots));
	}

	public DefenseModule(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseDefenseModule(stmnt));
	}

}
