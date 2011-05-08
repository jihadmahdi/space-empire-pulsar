package org.axan.sep.common.db.sqlite.orm;

import org.axan.sep.common.db.sqlite.orm.ISpecialUnit;
import org.axan.sep.common.db.sqlite.orm.base.IBaseHero;
import com.almworks.sqlite4java.SQLiteStatement;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eSpecialUnitType;

public interface IHero extends ISpecialUnit
{
	public Integer getExperience();
}
