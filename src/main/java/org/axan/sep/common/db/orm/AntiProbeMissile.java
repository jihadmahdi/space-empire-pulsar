package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Unit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseAntiProbeMissile;
import org.axan.sep.common.db.orm.base.BaseAntiProbeMissile;
import org.axan.sep.common.db.IAntiProbeMissile;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;

public class AntiProbeMissile extends Unit implements IAntiProbeMissile
{
	private final IBaseAntiProbeMissile baseAntiProbeMissileProxy;

	AntiProbeMissile(IBaseAntiProbeMissile baseAntiProbeMissileProxy, IGameConfig config)
	{
		super(baseAntiProbeMissileProxy, config);
		this.baseAntiProbeMissileProxy = baseAntiProbeMissileProxy;
	}

	public AntiProbeMissile(String owner, String name, eUnitType type, IGameConfig config)
	{
		this(new BaseAntiProbeMissile(owner, name, type.toString()), config);
	}

	public AntiProbeMissile(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseAntiProbeMissile(stmnt), config);
	}

}
