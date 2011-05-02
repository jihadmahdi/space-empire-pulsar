package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.Probe;
import org.axan.sep.server.model.orm.VersionedUnit;
import org.axan.sep.server.model.orm.base.BaseVersionedProbe;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eUnitType;
import com.almworks.sqlite4java.SQLiteStatement;

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
