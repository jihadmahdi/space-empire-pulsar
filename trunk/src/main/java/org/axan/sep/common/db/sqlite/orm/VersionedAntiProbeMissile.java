package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.AntiProbeMissile;
import org.axan.sep.common.db.sqlite.orm.VersionedUnit;
import org.axan.sep.common.db.sqlite.orm.base.BaseVersionedAntiProbeMissile;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public class VersionedAntiProbeMissile implements IVersionedAntiProbeMissile
{
	private final AntiProbeMissile antiProbeMissileProxy;
	private final VersionedUnit versionedUnitProxy;
	private final BaseVersionedAntiProbeMissile baseVersionedAntiProbeMissileProxy;

	public VersionedAntiProbeMissile(String owner, String name, eUnitType type, Integer turn, Location departure, Double progress, Location destination, String targetOwner, String targetName, Integer targetTurn, float sight)
	{
		antiProbeMissileProxy = new AntiProbeMissile(owner, name, type, sight);
		versionedUnitProxy = new VersionedUnit(owner, name, type, turn, departure, progress, destination, sight);
		baseVersionedAntiProbeMissileProxy = new BaseVersionedAntiProbeMissile(owner, name, type.toString(), turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, targetOwner, targetName, targetTurn);
	}

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

	public String getTargetOwner()
	{
		return baseVersionedAntiProbeMissileProxy.getTargetOwner();
	}

	public String getTargetName()
	{
		return baseVersionedAntiProbeMissileProxy.getTargetName();
	}

	public Integer getTargetTurn()
	{
		return baseVersionedAntiProbeMissileProxy.getTargetTurn();
	}

	public String getOwner()
	{
		return antiProbeMissileProxy.getOwner();
	}

	public String getName()
	{
		return antiProbeMissileProxy.getName();
	}

	public eUnitType getType()
	{
		return antiProbeMissileProxy.getType();
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
