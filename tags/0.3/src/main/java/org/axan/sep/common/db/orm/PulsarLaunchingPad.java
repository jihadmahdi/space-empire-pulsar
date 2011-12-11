package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IPulsarLaunchingPad;
import org.axan.sep.common.db.orm.base.BasePulsarLaunchingPad;
import org.axan.sep.common.db.orm.base.IBasePulsarLaunchingPad;

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

	@Override
	public Integer getFiredDate()
	{
		return basePulsarLaunchingPadProxy.getFiredDate();
	}

}
