package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.AntiProbeMissile;
import org.axan.sep.server.model.orm.VersionedUnit;
import org.axan.sep.server.model.orm.base.BaseVersionedAntiProbeMissile;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.Protocol.eUnitType;
import com.almworks.sqlite4java.SQLiteStatement;

public class VersionedAntiProbeMissile implements IVersionedAntiProbeMissile
{
	private final AntiProbeMissile antiProbeMissileProxy;
	private final VersionedUnit versionedUnitProxy;
	private final BaseVersionedAntiProbeMissile baseVersionedAntiProbeMissileProxy;

	public VersionedAntiProbeMissile(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		this.antiProbeMissileProxy = new AntiProbeMissile(stmnt, config);
		this.versionedUnitProxy = new VersionedUnit(stmnt, config);
		this.baseVersionedAntiProbeMissileProxy = new BaseVersionedAntiProbeMissile(stmnt);
	}

	@Override
	public float getSight()
	{
		return versionedUnitProxy.getSight();
	}
	
	public String getTargetName()
	{
		return baseVersionedAntiProbeMissileProxy.getTargetName();
	}

	public Integer getTargetTurn()
	{
		return baseVersionedAntiProbeMissileProxy.getTargetTurn();
	}

	public String getTargetOwner()
	{
		return baseVersionedAntiProbeMissileProxy.getTargetOwner();
	}

	public String getOwner()
	{
		return antiProbeMissileProxy.getOwner();
	}

	public eUnitType getType()
	{
		return antiProbeMissileProxy.getType();
	}

	public String getName()
	{
		return antiProbeMissileProxy.getName();
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

}
