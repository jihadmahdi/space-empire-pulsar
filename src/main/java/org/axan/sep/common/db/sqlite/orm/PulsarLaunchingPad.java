package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Building;
import org.axan.sep.common.db.sqlite.orm.base.BasePulsarLaunchingPad;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eBuildingType;

public class PulsarLaunchingPad extends Building implements IPulsarLaunchingPad
{
	private final BasePulsarLaunchingPad basePulsarLaunchingPadProxy;

	public PulsarLaunchingPad(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots, Integer firedDate)
	{
		super(type, celestialBodyName, turn, nbSlots);
		basePulsarLaunchingPadProxy = new BasePulsarLaunchingPad(type.toString(), celestialBodyName, turn, nbSlots, firedDate);
	}

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
