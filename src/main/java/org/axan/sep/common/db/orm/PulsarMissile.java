package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Unit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBasePulsarMissile;
import org.axan.sep.common.db.orm.base.BasePulsarMissile;
import org.axan.sep.common.db.IPulsarMissile;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;

public class PulsarMissile extends Unit implements IPulsarMissile
{
	private final IBasePulsarMissile basePulsarMissileProxy;

	PulsarMissile(IBasePulsarMissile basePulsarMissileProxy, IGameConfig config)
	{
		super(basePulsarMissileProxy, config);
		this.basePulsarMissileProxy = basePulsarMissileProxy;
	}

	public PulsarMissile(String owner, String name, eUnitType type, Integer time, Integer volume, IGameConfig config)
	{
		this(new BasePulsarMissile(owner, name, type.toString(), time, volume), config);
	}

	public PulsarMissile(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BasePulsarMissile(stmnt), config);
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
