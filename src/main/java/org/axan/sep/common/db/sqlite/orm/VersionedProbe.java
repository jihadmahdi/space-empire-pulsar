package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Probe;
import org.axan.sep.common.db.sqlite.orm.VersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedProbe;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public class VersionedProbe implements IVersionedProbe
{
	private final Probe probeProxy;
	private final VersionedUnit versionedUnitProxy;
	private final BaseVersionedProbe baseVersionedProbeProxy;

	public VersionedProbe(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.probeProxy = new Probe(stmnt, config);
		this.versionedUnitProxy = new VersionedUnit(stmnt, config);
		this.baseVersionedProbeProxy = new BaseVersionedProbe(stmnt);
	}

	@Override
	public float getSight()
	{
		return versionedUnitProxy.getSight();
	}
	
	public String getOwner()
	{
		return probeProxy.getOwner();
	}

	public eUnitType getType()
	{
		return probeProxy.getType();
	}

	public String getName()
	{
		return probeProxy.getName();
	}

	public Location getDeparture()
	{
		return versionedUnitProxy.getDeparture();
	}

	public Double getProgress()
	{
		return versionedUnitProxy.getProgress();
	}

	public Location getDestination()
	{
		return versionedUnitProxy.getDestination();
	}

	public Integer getTurn()
	{
		return versionedUnitProxy.getTurn();
	}

	@Override
	public boolean isDeployed()
	{
		return getProgress() == 100.0;
	}
}
