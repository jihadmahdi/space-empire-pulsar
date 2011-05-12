package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Unit;
import org.axan.sep.common.db.sqlite.orm.base.BasePulsarMissile;
import org.axan.sep.common.db.IPulsarMissile;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;

public class PulsarMissile extends Unit implements IPulsarMissile
{
	private final BasePulsarMissile basePulsarMissileProxy;

	public PulsarMissile(String owner, String name, eUnitType type, Integer time, Integer volume, float sight)
	{
		super(owner, name, type, sight);
		basePulsarMissileProxy = new BasePulsarMissile(owner, name, type.toString(), time, volume);
	}

	public PulsarMissile(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.basePulsarMissileProxy = new BasePulsarMissile(stmnt);
	}

	public Integer getTime()
	{
		return basePulsarMissileProxy.getTime();
	}

	public Integer getVolume()
	{
		return basePulsarMissileProxy.getVolume();
	}

}
