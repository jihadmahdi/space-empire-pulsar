package org.axan.sep.common.db.orm;

import org.axan.sep.common.db.orm.SpecialUnit;
import java.lang.Exception;
import org.axan.sep.common.db.orm.base.IBaseHero;
import org.axan.sep.common.db.orm.base.BaseHero;
import org.axan.sep.common.db.IHero;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.db.IGameConfig;

public class Hero extends SpecialUnit implements IHero
{
	private final IBaseHero baseHeroProxy;

	Hero(IBaseHero baseHeroProxy)
	{
		super(baseHeroProxy);
		this.baseHeroProxy = baseHeroProxy;
	}

	public Hero(String owner, String name, eSpecialUnitType type, Integer experience)
	{
		this(new BaseHero(owner, name, type.toString(), experience));
	}

	public Hero(ISQLDataBaseStatement stmnt) throws Exception
	{
		this(new BaseHero(stmnt));
	}

	public Integer getExperience()
	{
		return baseHeroProxy.getExperience();
	}

}
