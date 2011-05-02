package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.Unit;
import org.axan.sep.server.model.orm.base.BaseProbe;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class Probe extends Unit implements IProbe
{
	private final BaseProbe baseProbeProxy;

	public Probe(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseProbeProxy = new BaseProbe(stmnt);
	}

}
