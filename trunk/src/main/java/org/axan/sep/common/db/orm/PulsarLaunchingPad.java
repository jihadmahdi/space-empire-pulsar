package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Building;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBasePulsarLaunchingPad;
import org.axan.sep.common.db.orm.base.BasePulsarLaunchingPad;
import org.axan.sep.common.db.IPulsarLaunchingPad;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

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

	public PulsarLaunchingPad(Node stmnt) throws Exception
	{
		this(new BasePulsarLaunchingPad(stmnt));
	}

	@Override
	public Integer getFiredDate()
	{
		return basePulsarLaunchingPadProxy.getFiredDate();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return basePulsarLaunchingPadProxy.getNode();
	}

}
