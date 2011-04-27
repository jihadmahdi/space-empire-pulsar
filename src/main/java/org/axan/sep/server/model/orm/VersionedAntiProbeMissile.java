package org.axan.sep.server.model.orm;

import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.model.orm.AntiProbeMissile;
import org.axan.sep.server.model.orm.VersionedUnit;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.server.model.orm.base.BaseVersionedAntiProbeMissile;

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
	
	@Override
	public Double getProgress()
	{
		return versionedUnitProxy.getProgress();
	}

	public Integer getTurn()
	{
		return versionedUnitProxy.getTurn();
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

}
