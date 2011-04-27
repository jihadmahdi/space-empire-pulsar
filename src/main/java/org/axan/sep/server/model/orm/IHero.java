package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.ISpecialUnit;
import org.axan.sep.server.model.orm.base.IBaseHero;

public interface IHero extends ISpecialUnit
{
	public Integer getExperience();
}
