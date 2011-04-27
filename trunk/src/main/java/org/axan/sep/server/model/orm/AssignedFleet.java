package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseAssignedFleet;

public class AssignedFleet implements IAssignedFleet
{
	private final BaseAssignedFleet baseAssignedFleetProxy;

	public AssignedFleet(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseAssignedFleetProxy = new BaseAssignedFleet(stmnt);
	}

	public String getFleetName()
	{
		return baseAssignedFleetProxy.getFleetName();
	}

	public String getOwner()
	{
		return baseAssignedFleetProxy.getOwner();
	}

	public String getCelestialBody()
	{
		return baseAssignedFleetProxy.getCelestialBody();
	}

}
