package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Unit;
import org.axan.sep.common.db.sqlite.orm.base.BaseProbe;
import org.axan.sep.common.db.IProbe;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;

public class Probe extends Unit implements IProbe
{
	private final BaseProbe baseProbeProxy;

	public Probe(String owner, String name, eUnitType type, float sight)
	{
		super(owner, name, type, sight);
		baseProbeProxy = new BaseProbe(owner, name, type.toString());
	}

	public Probe(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseProbeProxy = new BaseProbe(stmnt);
	}

}
