package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseArea;

public class Area implements IArea
{
	private final BaseArea baseAreaProxy;

	public Area(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseAreaProxy = new BaseArea(stmnt);
	}

	public Boolean getIsSun()
	{
		return baseAreaProxy.getIsSun();
	}

	public Integer getLocation_y()
	{
		return baseAreaProxy.getLocation_y();
	}

	public Integer getLocation_x()
	{
		return baseAreaProxy.getLocation_x();
	}

	public Integer getLocation_z()
	{
		return baseAreaProxy.getLocation_z();
	}

}
