package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseUnitEncounterLog;
import org.axan.sep.common.db.orm.base.BaseUnitEncounterLog;
import org.axan.sep.common.db.IUnitEncounterLog;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class UnitEncounterLog implements IUnitEncounterLog
{
	private final IBaseUnitEncounterLog baseUnitEncounterLogProxy;

	UnitEncounterLog(IBaseUnitEncounterLog baseUnitEncounterLogProxy)
	{
		this.baseUnitEncounterLogProxy = baseUnitEncounterLogProxy;
	}

	public UnitEncounterLog(String owner, String unitName, String unitType, Integer logTurn, Integer instantTime, String seenOwner, String seenName, Integer seenTurn, String seenType)
	{
		this(new BaseUnitEncounterLog(owner, unitName, unitType, logTurn, instantTime, seenOwner, seenName, seenTurn, seenType));
	}

	public UnitEncounterLog(Node stmnt) throws Exception
	{
		this(new BaseUnitEncounterLog(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseUnitEncounterLogProxy.getOwner();
	}

	@Override
	public String getUnitName()
	{
		return baseUnitEncounterLogProxy.getUnitName();
	}

	@Override
	public String getUnitType()
	{
		return baseUnitEncounterLogProxy.getUnitType();
	}

	@Override
	public Integer getLogTurn()
	{
		return baseUnitEncounterLogProxy.getLogTurn();
	}

	@Override
	public Integer getInstantTime()
	{
		return baseUnitEncounterLogProxy.getInstantTime();
	}

	@Override
	public String getSeenOwner()
	{
		return baseUnitEncounterLogProxy.getSeenOwner();
	}

	@Override
	public String getSeenName()
	{
		return baseUnitEncounterLogProxy.getSeenName();
	}

	@Override
	public Integer getSeenTurn()
	{
		return baseUnitEncounterLogProxy.getSeenTurn();
	}

	@Override
	public String getSeenType()
	{
		return baseUnitEncounterLogProxy.getSeenType();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseUnitEncounterLogProxy.getNode();
	}

}
