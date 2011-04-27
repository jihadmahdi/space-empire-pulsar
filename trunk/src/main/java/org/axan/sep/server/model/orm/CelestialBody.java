package org.axan.sep.server.model.orm;

import com.almworks.sqlite4java.SQLiteStatement;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.server.model.orm.base.BaseCelestialBody;

public class CelestialBody implements ICelestialBody
{
	private final BaseCelestialBody baseCelestialBodyProxy;

	public CelestialBody(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.baseCelestialBodyProxy = new BaseCelestialBody(stmnt);
	}

	public Integer getLocation_y()
	{
		return baseCelestialBodyProxy.getLocation_y();
	}

	public Integer getLocation_x()
	{
		return baseCelestialBodyProxy.getLocation_x();
	}

	public String getType()
	{
		return baseCelestialBodyProxy.getType();
	}

	public String getName()
	{
		return baseCelestialBodyProxy.getName();
	}

	public Integer getLocation_z()
	{
		return baseCelestialBodyProxy.getLocation_z();
	}

}
