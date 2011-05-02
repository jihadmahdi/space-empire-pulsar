package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.SpecialUnit;
import org.axan.sep.server.model.orm.base.BaseVersionedSpecialUnit;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class VersionedSpecialUnit extends SpecialUnit implements IVersionedSpecialUnit
{
	private final BaseVersionedSpecialUnit baseVersionedSpecialUnitProxy;

	public VersionedSpecialUnit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
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

	public Integer getTurn()
	{
		return baseVersionedSpecialUnitProxy.getTurn();
	}

}
