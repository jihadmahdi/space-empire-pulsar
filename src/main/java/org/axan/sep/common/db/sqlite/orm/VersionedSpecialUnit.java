package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.SpecialUnit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedSpecialUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eSpecialUnitType;

public class VersionedSpecialUnit extends SpecialUnit implements IVersionedSpecialUnit
{
	private final BaseVersionedSpecialUnit baseVersionedSpecialUnitProxy;

	public VersionedSpecialUnit(String owner, String name, eSpecialUnitType type, Integer turn, String fleetOwner, String fleetName, Integer fleetTurn)
	{
		super(owner, name, type);
		baseVersionedSpecialUnitProxy = new BaseVersionedSpecialUnit(owner, name, type.toString(), turn, fleetOwner, fleetName, fleetTurn);
	}

	public VersionedSpecialUnit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseVersionedSpecialUnitProxy = new BaseVersionedSpecialUnit(stmnt);
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
