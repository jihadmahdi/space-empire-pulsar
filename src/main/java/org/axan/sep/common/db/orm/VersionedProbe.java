package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedProbe;
import org.axan.sep.common.db.orm.base.BaseVersionedProbe;
import org.axan.sep.common.db.IVersionedProbe;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedProbe extends VersionedUnit implements IVersionedProbe
{
	private final IBaseVersionedProbe baseVersionedProbeProxy;

	VersionedProbe(IBaseVersionedProbe baseVersionedProbeProxy, IGameConfig config)
	{
		super(baseVersionedProbeProxy, config);
		this.baseVersionedProbeProxy = baseVersionedProbeProxy;
	}

	public VersionedProbe(String owner, String name, eUnitType type, Integer turn, Location departure, Double progress, Location destination, IGameConfig config)
	{
		this(new BaseVersionedProbe(owner, name, type.toString(), turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z), config);
	}

	public VersionedProbe(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseVersionedProbe(stmnt), config);
	}

	@Override
	public boolean isDeployed()
	{
		return getProgress() == 100.0;
	}
}
