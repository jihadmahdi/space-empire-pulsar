package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ISpecialUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBaseHero;
import org.axan.sep.common.IGameConfig;
import com.almworks.sqlite4java.SQLiteStatement;

public interface IHero extends ISpecialUnit
{
	public Integer getExperience();
}
