package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.server.model.orm.base.BaseUnit;

public class Unit implements IUnit
{
	private final BaseUnit baseUnitProxy;
	
	private final float speed;
	private final float sight;

	public Unit(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseUnitProxy = new BaseUnit(stmnt);
		this.speed = config.getUnitTypeSpeed(getType());
		this.sight = config.getUnitTypeSight(getType());
	}
	
	public eUnitType getType()
	{
		return eUnitType.valueOf(baseUnitProxy.getType());
	}
	
	@Override
	public float getSpeed()
	{
		return speed;
	}
	
	@Override
	public float getSight()
	{
		return sight;
	}
	
	// Delegates

	public String getOwner()
	{
		return baseUnitProxy.getOwner();
	}

	public String getName()
	{
		return baseUnitProxy.getName();
	}

}
