package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.model.orm.Probe;
import org.axan.sep.server.model.orm.VersionedUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseVersionedProbe;

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

	public String getOwner()
	{
		return probeProxy.getOwner();
	}

	public eUnitType getType()
	{
		return probeProxy.getType();
	}
	
	@Override
	public float getSight()
	{
		return versionedUnitProxy.getSight();
	}

	@Override
	public float getSpeed()
	{
		return versionedUnitProxy.getSpeed();
	}

	public String getName()
	{
		return probeProxy.getName();
	}

	public Double getProgress()
	{
		return versionedUnitProxy.getProgress();
	}

	@Override
	public RealLocation getDeparture()
	{
		return versionedUnitProxy.getDeparture();
	}
	
	@Override
	public RealLocation getDestination()
	{
		return versionedUnitProxy.getDestination();
	}

	public Integer getTurn()
	{
		return versionedUnitProxy.getTurn();
	}

}
