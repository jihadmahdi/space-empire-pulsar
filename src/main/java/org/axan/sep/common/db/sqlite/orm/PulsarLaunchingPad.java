package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Building;
import org.axan.sep.common.db.sqlite.orm.base.BasePulsarLaunchingPad;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

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
