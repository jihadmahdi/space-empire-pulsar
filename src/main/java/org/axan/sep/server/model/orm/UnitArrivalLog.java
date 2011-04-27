package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseUnitArrivalLog;

public class UnitArrivalLog implements IUnitArrivalLog
{
	private final BaseUnitArrivalLog baseUnitArrivalLogProxy;

	public UnitArrivalLog(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseUnitArrivalLogProxy = new BaseUnitArrivalLog(stmnt);
	}

	public String getUnitType()
	{
		return baseUnitArrivalLogProxy.getUnitType();
	}

	public Integer getUnitTurn()
	{
		return baseUnitArrivalLogProxy.getUnitTurn();
	}

	public String getDestination()
	{
		return baseUnitArrivalLogProxy.getDestination();
	}

	public String getUnitName()
	{
		return baseUnitArrivalLogProxy.getUnitName();
	}

	public Integer getInstantTime()
	{
		return baseUnitArrivalLogProxy.getInstantTime();
	}

	public String getUnitOwner()
	{
		return baseUnitArrivalLogProxy.getUnitOwner();
	}

	public String getVortex()
	{
		return baseUnitArrivalLogProxy.getVortex();
	}

}
