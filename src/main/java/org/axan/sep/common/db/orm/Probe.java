package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Unit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseProbe;
import org.axan.sep.common.db.orm.base.BaseProbe;
import org.axan.sep.common.db.IProbe;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;

public class Probe extends Unit implements IProbe
{
	private final IBaseProbe baseProbeProxy;

	Probe(IBaseProbe baseProbeProxy, IGameConfig config)
	{
		super(baseProbeProxy, config);
		this.baseProbeProxy = baseProbeProxy;
	}

	public Probe(String owner, String name, eUnitType type, IGameConfig config)
	{
		this(new BaseProbe(owner, name, type.toString()), config);
	}

	public Probe(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseProbe(stmnt), config);
	}

}
