package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.SpecialUnit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedSpecialUnit;
import org.axan.sep.common.db.orm.base.BaseVersionedSpecialUnit;
import org.axan.sep.common.db.IVersionedSpecialUnit;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.db.IGameConfig;

public class VersionedSpecialUnit extends SpecialUnit implements IVersionedSpecialUnit
{
	private final IBaseVersionedSpecialUnit baseVersionedSpecialUnitProxy;

	VersionedSpecialUnit(IBaseVersionedSpecialUnit baseVersionedSpecialUnitProxy)
	{
		super(baseVersionedSpecialUnitProxy);
		this.baseVersionedSpecialUnitProxy = baseVersionedSpecialUnitProxy;
	}

	public VersionedSpecialUnit(String owner, String name, eSpecialUnitType type, Integer turn, String fleetOwner, String fleetName, Integer fleetTurn)
	{
		this(new BaseVersionedSpecialUnit(owner, name, type.toString(), turn, fleetOwner, fleetName, fleetTurn));
	}

	public VersionedSpecialUnit(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseVersionedSpecialUnit(stmnt));
	}

	public Integer getTurn()
	{
		return baseVersionedSpecialUnitProxy.getTurn();
	}

	public String getFleetOwner()
	{
		return baseVersionedSpecialUnitProxy.getFleetOwner();
	}

	public String getFleetName()
	{
		return baseVersionedSpecialUnitProxy.getFleetName();
	}

	public Integer getFleetTurn()
	{
		return baseVersionedSpecialUnitProxy.getFleetTurn();
	}

}
