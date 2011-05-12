package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.Probe;
import org.axan.sep.common.db.sqlite.orm.VersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedProbe;
import org.axan.sep.common.db.IVersionedProbe;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedProbe implements IVersionedProbe
{
	private final Probe probeProxy;
	private final VersionedUnit versionedUnitProxy;
	private final BaseVersionedProbe baseVersionedProbeProxy;

	public VersionedProbe(String owner, String name, eUnitType type, Integer turn, Location departure, Double progress, Location destination, float sight)
	{
		probeProxy = new Probe(owner, name, type, sight);
		versionedUnitProxy = new VersionedUnit(owner, name, type, turn, departure, progress, destination, sight);
		baseVersionedProbeProxy = new BaseVersionedProbe(owner, name, type.toString(), turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z);
	}

	public VersionedProbe(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.probeProxy = new Probe(stmnt, config);
		this.versionedUnitProxy = new VersionedUnit(stmnt, config);
		this.baseVersionedProbeProxy = new BaseVersionedProbe(stmnt);
	}

	@Override
	public boolean isDeployed()
	{
		return getProgress() == 100.0;
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

	public String getName()
	{
		return probeProxy.getName();
	}

	public eUnitType getType()
	{
		return probeProxy.getType();
	}

	public Integer getTurn()
	{
		return versionedUnitProxy.getTurn();
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

}
