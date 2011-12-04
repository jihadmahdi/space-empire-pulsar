package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseVersionedAntiProbeMissile;
import org.axan.sep.common.db.orm.base.BaseVersionedAntiProbeMissile;
import org.axan.sep.common.db.IVersionedAntiProbeMissile;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;

public class VersionedAntiProbeMissile extends VersionedUnit implements IVersionedAntiProbeMissile
{
	private final IBaseVersionedAntiProbeMissile baseVersionedAntiProbeMissileProxy;

	VersionedAntiProbeMissile(IBaseVersionedAntiProbeMissile baseVersionedAntiProbeMissileProxy, IGameConfig config)
	{
		super(baseVersionedAntiProbeMissileProxy, config);
		this.baseVersionedAntiProbeMissileProxy = baseVersionedAntiProbeMissileProxy;
	}

	public VersionedAntiProbeMissile(String owner, String name, eUnitType type, Integer turn, Location departure, Double progress, Location destination, String targetOwner, String targetName, Integer targetTurn, IGameConfig config)
	{
		this(new BaseVersionedAntiProbeMissile(owner, name, type.toString(), turn, departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, targetOwner, targetName, targetTurn), config);
	}

	public VersionedAntiProbeMissile(ISQLDataBaseStatement stmnt, IGameConfig config) throws Exception
	{
		this(new BaseVersionedAntiProbeMissile(stmnt), config);
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

}
