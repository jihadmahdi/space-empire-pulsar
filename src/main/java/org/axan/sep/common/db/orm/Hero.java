package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.SpecialUnit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseHero;
import org.axan.sep.common.db.orm.base.BaseHero;
import org.axan.sep.common.db.IHero;
import java.util.HashMap;
import java.util.Map;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.db.IGameConfig;
import org.neo4j.graphdb.Node;

public class Hero extends SpecialUnit implements IHero
{
	private final IBaseHero baseHeroProxy;

	Hero(IBaseHero baseHeroProxy)
	{
		super(baseHeroProxy);
		this.baseHeroProxy = baseHeroProxy;
	}

	public Hero(String owner, String name, eSpecialUnitType type, String fleetName, Integer experience)
	{
		this(new BaseHero(owner, name, type.toString(), fleetName, experience));
	}

	public Hero(Node stmnt) throws Exception
	{
		this(new BaseHero(stmnt));
	}

	@Override
	public Integer getExperience()
	{
		return baseHeroProxy.getExperience();
	}

	@Override
	public Map<String, Object> getNode()
	{
		return baseHeroProxy.getNode();
	}

}
