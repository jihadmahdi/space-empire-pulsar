package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Unit;
import org.axan.sep.common.db.sqlite.orm.base.BaseAntiProbeMissile;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class AntiProbeMissile extends Unit implements IAntiProbeMissile
{
	private final BaseAntiProbeMissile baseAntiProbeMissileProxy;

	public AntiProbeMissile(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseAntiProbeMissileProxy = new BaseAntiProbeMissile(stmnt);
	}

}
