package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.CelestialBody;
import org.axan.sep.server.model.orm.base.BaseVortex;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class Vortex extends CelestialBody implements IVortex
{
	private final BaseVortex baseVortexProxy;

	public Vortex(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseVortexProxy = new BaseVortex(stmnt);
	}

	public Integer getOnsetDate()
	{
		return baseVortexProxy.getOnsetDate();
	}

	public Integer getEndDate()
	{
		return baseVortexProxy.getEndDate();
	}

	public String getDestination()
	{
		return baseVortexProxy.getDestination();
	}

}
