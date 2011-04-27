package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.Building;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BasePulsarLaunchingPad;

public class PulsarLaunchingPad extends Building implements IPulsarLaunchingPad
{
	private final BasePulsarLaunchingPad basePulsarLaunchingPadProxy;

	public PulsarLaunchingPad(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.basePulsarLaunchingPadProxy = new BasePulsarLaunchingPad(stmnt);
	}

	public Integer getFiredDate()
	{
		return basePulsarLaunchingPadProxy.getFiredDate();
	}

}
