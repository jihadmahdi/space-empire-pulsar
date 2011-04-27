package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.Unit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseProbe;

public class Probe extends Unit implements IProbe
{
	private final BaseProbe baseProbeProxy;

	public Probe(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseProbeProxy = new BaseProbe(stmnt);
	}

}
