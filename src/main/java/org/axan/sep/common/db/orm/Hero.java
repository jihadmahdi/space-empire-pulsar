package org.axan.sep.common.db.orm;

import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.db.IHero;
import org.axan.sep.common.db.orm.base.BaseHero;
import org.axan.sep.common.db.orm.base.IBaseHero;

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

	public Hero(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseHero(stmnt));
	}

	@Override
	public Integer getExperience()
	{
		return baseHeroProxy.getExperience();
	}

}
