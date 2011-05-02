package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.SpecialUnit;
import org.axan.sep.server.model.orm.base.BaseHero;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public class Hero extends SpecialUnit implements IHero
{
	private final BaseHero baseHeroProxy;

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
