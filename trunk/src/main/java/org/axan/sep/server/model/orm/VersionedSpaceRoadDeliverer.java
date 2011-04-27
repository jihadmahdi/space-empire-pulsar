package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseVersionedSpaceRoadDeliverer;

public class VersionedSpaceRoadDeliverer implements IVersionedSpaceRoadDeliverer
{
	private final BaseVersionedSpaceRoadDeliverer baseVersionedSpaceRoadDelivererProxy;

	public VersionedSpaceRoadDeliverer(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseVersionedSpaceRoadDelivererProxy = new BaseVersionedSpaceRoadDeliverer(stmnt);
	}

	public String getOwner()
	{
		return baseVersionedSpaceRoadDelivererProxy.getOwner();
	}

	public Integer getTurn()
	{
		return baseVersionedSpaceRoadDelivererProxy.getTurn();
	}

	public String getType()
	{
		return baseVersionedSpaceRoadDelivererProxy.getType();
	}

	public String getName()
	{
		return baseVersionedSpaceRoadDelivererProxy.getName();
	}

}
