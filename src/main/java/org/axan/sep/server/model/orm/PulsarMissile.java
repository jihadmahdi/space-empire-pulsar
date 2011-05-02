package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.Unit;
import org.axan.sep.server.model.orm.base.BasePulsarMissile;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class PulsarMissile extends Unit implements IPulsarMissile
{
	private final BasePulsarMissile basePulsarMissileProxy;

	public PulsarMissile(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.basePulsarMissileProxy = new BasePulsarMissile(stmnt);
	}

	public Integer getVolume()
	{
		return basePulsarMissileProxy.getVolume();
	}

	public Integer getTime()
	{
		return basePulsarMissileProxy.getTime();
	}

}
