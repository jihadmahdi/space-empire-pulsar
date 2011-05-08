package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Unit;
import org.axan.sep.common.db.sqlite.orm.base.BaseAntiProbeMissile;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;

public class AntiProbeMissile extends Unit implements IAntiProbeMissile
{
	private final BaseAntiProbeMissile baseAntiProbeMissileProxy;

	public AntiProbeMissile(String owner, String name, eUnitType type, float sight)
	{
		super(owner, name, type, sight);
		baseAntiProbeMissileProxy = new BaseAntiProbeMissile(owner, name, type.toString());
	}

	public AntiProbeMissile(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseAntiProbeMissileProxy = new BaseAntiProbeMissile(stmnt);
	}

}
