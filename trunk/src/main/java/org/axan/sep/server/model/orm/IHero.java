package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.ISpecialUnit;
import org.axan.sep.server.model.orm.base.IBaseHero;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IHero extends ISpecialUnit
{
	public Integer getExperience();
}
