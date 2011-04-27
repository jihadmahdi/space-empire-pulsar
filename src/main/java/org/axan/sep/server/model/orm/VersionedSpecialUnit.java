package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseVersionedSpecialUnit;

public class VersionedSpecialUnit implements IVersionedSpecialUnit
{
	private final BaseVersionedSpecialUnit baseVersionedSpecialUnitProxy;

	public VersionedSpecialUnit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseVersionedSpecialUnitProxy = new BaseVersionedSpecialUnit(stmnt);
	}

	public String getFleetName()
	{
		return baseVersionedSpecialUnitProxy.getFleetName();
	}

	public Integer getFleetTurn()
	{
		return baseVersionedSpecialUnitProxy.getFleetTurn();
	}

	public String getFleetOwner()
	{
		return baseVersionedSpecialUnitProxy.getFleetOwner();
	}

	public String getOwner()
	{
		return baseVersionedSpecialUnitProxy.getOwner();
	}

	public Integer getTurn()
	{
		return baseVersionedSpecialUnitProxy.getTurn();
	}

	public String getType()
	{
		return baseVersionedSpecialUnitProxy.getType();
	}

	public String getName()
	{
		return baseVersionedSpecialUnitProxy.getName();
	}

}
