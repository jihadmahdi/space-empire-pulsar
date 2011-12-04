package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Building;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBasePulsarLaunchingPad;
import org.axan.sep.common.db.orm.base.BasePulsarLaunchingPad;
import org.axan.sep.common.db.IPulsarLaunchingPad;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;

public class PulsarLaunchingPad extends Building implements IPulsarLaunchingPad
{
	private final IBasePulsarLaunchingPad basePulsarLaunchingPadProxy;

	PulsarLaunchingPad(IBasePulsarLaunchingPad basePulsarLaunchingPadProxy)
	{
		super(basePulsarLaunchingPadProxy);
		this.basePulsarLaunchingPadProxy = basePulsarLaunchingPadProxy;
	}

	public PulsarLaunchingPad(eBuildingType type, String celestialBodyName, Integer turn, Integer nbSlots, Integer firedDate)
	{
		this(new BasePulsarLaunchingPad(type.toString(), celestialBodyName, turn, nbSlots, firedDate));
	}

	public PulsarLaunchingPad(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BasePulsarLaunchingPad(stmnt));
	}

	public Integer getFiredDate()
	{
		return basePulsarLaunchingPadProxy.getFiredDate();
	}

}
