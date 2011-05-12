package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.SpecialUnit;
import org.axan.sep.common.db.sqlite.orm.base.BaseHero;
import org.axan.sep.common.db.IHero;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.db.IGameConfig;

public class Hero extends SpecialUnit implements IHero
{
	private final BaseHero baseHeroProxy;

	public Hero(String owner, String name, eSpecialUnitType type, Integer experience)
	{
		super(owner, name, type);
		baseHeroProxy = new BaseHero(owner, name, type.toString(), experience);
	}

	public Hero(SQLiteStatement stmnt, IGameConfig config) throws Exception
	{
		super(stmnt, config);
		this.baseHeroProxy = new BaseHero(stmnt);
	}

	public Integer getExperience()
	{
		return baseHeroProxy.getExperience();
	}

}
