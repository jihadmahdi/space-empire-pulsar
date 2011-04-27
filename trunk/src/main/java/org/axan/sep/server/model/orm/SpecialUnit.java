package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseSpecialUnit;

public class SpecialUnit implements ISpecialUnit
{
	private final BaseSpecialUnit baseSpecialUnitProxy;

	public SpecialUnit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseSpecialUnitProxy = new BaseSpecialUnit(stmnt);
	}

	public String getOwner()
	{
		return baseSpecialUnitProxy.getOwner();
	}

	public String getType()
	{
		return baseSpecialUnitProxy.getType();
	}

	public String getName()
	{
		return baseSpecialUnitProxy.getName();
	}

}
