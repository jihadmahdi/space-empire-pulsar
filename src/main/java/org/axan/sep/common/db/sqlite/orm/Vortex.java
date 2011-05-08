package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.CelestialBody;
import org.axan.sep.common.db.sqlite.orm.base.BaseVortex;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;

public class Vortex extends CelestialBody implements IVortex
{
	private final BaseVortex baseVortexProxy;

	public Vortex(String name, eCelestialBodyType type, Location location, Integer onsetDate, Integer endDate, String destination)
	{
		super(name, type, location);
		baseVortexProxy = new BaseVortex(name, type.toString(), location == null ? null : location.x, location == null ? null : location.y, location == null ? null : location.z, onsetDate, endDate, destination);
	}

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
