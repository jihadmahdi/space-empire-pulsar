package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseUnitEncounterLog;

public class UnitEncounterLog implements IUnitEncounterLog
{
	private final BaseUnitEncounterLog baseUnitEncounterLogProxy;

	public UnitEncounterLog(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseUnitEncounterLogProxy = new BaseUnitEncounterLog(stmnt);
	}

	public Integer getSeenTurn()
	{
		return baseUnitEncounterLogProxy.getSeenTurn();
	}

	public String getSeenOwner()
	{
		return baseUnitEncounterLogProxy.getSeenOwner();
	}

	public String getUnitType()
	{
		return baseUnitEncounterLogProxy.getUnitType();
	}

	public Integer getUnitTurn()
	{
		return baseUnitEncounterLogProxy.getUnitTurn();
	}

	public String getSeenType()
	{
		return baseUnitEncounterLogProxy.getSeenType();
	}

	public String getUnitName()
	{
		return baseUnitEncounterLogProxy.getUnitName();
	}

	public Integer getInstantTime()
	{
		return baseUnitEncounterLogProxy.getInstantTime();
	}

	public String getUnitOwner()
	{
		return baseUnitEncounterLogProxy.getUnitOwner();
	}

	public String getSeenName()
	{
		return baseUnitEncounterLogProxy.getSeenName();
	}

}
