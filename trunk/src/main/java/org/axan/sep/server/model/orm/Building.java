package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.server.model.orm.base.BaseBuilding;

public class Building implements IBuilding
{
	private final BaseBuilding baseBuildingProxy;

	public Building(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseBuildingProxy = new BaseBuilding(stmnt);
	}
	
	@Override
	public eBuildingType getType()
	{
		return eBuildingType.valueOf(baseBuildingProxy.getType());
	}
	
	// Delegates
	
	public Integer getNbSlots()
	{
		return baseBuildingProxy.getNbSlots();
	}

	public String getCelestialBodyName()
	{
		return baseBuildingProxy.getCelestialBodyName();
	}

	public Integer getTurn()
	{
		return baseBuildingProxy.getTurn();
	}

}
