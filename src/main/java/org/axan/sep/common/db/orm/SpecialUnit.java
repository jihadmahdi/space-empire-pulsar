package org.axan.sep.common.db.orm;

import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseSpecialUnit;
import org.axan.sep.common.db.orm.base.BaseSpecialUnit;
import org.axan.sep.common.db.ISpecialUnit;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class SpecialUnit implements ISpecialUnit
{
	private final IBaseSpecialUnit baseSpecialUnitProxy;
	private final eSpecialUnitType type;

	SpecialUnit(IBaseSpecialUnit baseSpecialUnitProxy)
	{
		this.baseSpecialUnitProxy = baseSpecialUnitProxy;
		this.type = eSpecialUnitType.valueOf(baseSpecialUnitProxy.getType());
	}

	public SpecialUnit(String owner, String name, eSpecialUnitType type, String fleetName)
	{
		this(new BaseSpecialUnit(owner, name, type.toString(), fleetName));
	}

	public SpecialUnit(Node stmnt) throws Exception
	{
		this(new BaseSpecialUnit(stmnt));
	}

	@Override
	public String getOwner()
	{
		return baseSpecialUnitProxy.getOwner();
	}

	@Override
	public String getName()
	{
		return baseSpecialUnitProxy.getName();
	}

	@Override
	public eSpecialUnitType getType()
	{
		return type;
	}

	@Override
	public String getFleetName()
	{
		return baseSpecialUnitProxy.getFleetName();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseSpecialUnitProxy.getNode();
	}

}
