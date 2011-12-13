package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.Unit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseAntiProbeMissile;
import org.axan.sep.common.db.orm.base.BaseAntiProbeMissile;
import org.axan.sep.common.db.IAntiProbeMissile;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class AntiProbeMissile extends Unit implements IAntiProbeMissile
{
	private final IBaseAntiProbeMissile baseAntiProbeMissileProxy;

	AntiProbeMissile(IBaseAntiProbeMissile baseAntiProbeMissileProxy, IGameConfig config)
	{
		super(baseAntiProbeMissileProxy, config);
		this.baseAntiProbeMissileProxy = baseAntiProbeMissileProxy;
	}

	public AntiProbeMissile(String owner, String name, eUnitType type, Location departure, Double progress, Location destination, String targetOwner, String targetName, Integer targetTurn, IGameConfig config)
	{
		this(new BaseAntiProbeMissile(owner, name, type.toString(), departure == null ? null : departure.x, departure == null ? null : departure.y, departure == null ? null : departure.z, progress, destination == null ? null : destination.x, destination == null ? null : destination.y, destination == null ? null : destination.z, targetOwner, targetName, targetTurn), config);
	}

	public AntiProbeMissile(Node stmnt, IGameConfig config) throws Exception
	{
		this(new BaseAntiProbeMissile(stmnt), config);
	}

	@Override
	public String getTargetOwner()
	{
		return baseAntiProbeMissileProxy.getTargetOwner();
	}

	@Override
	public String getTargetName()
	{
		return baseAntiProbeMissileProxy.getTargetName();
	}

	@Override
	public Integer getTargetTurn()
	{
		return baseAntiProbeMissileProxy.getTargetTurn();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseAntiProbeMissileProxy.getNode();
	}

}
