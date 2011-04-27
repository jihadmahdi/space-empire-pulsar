package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseVersionedDiplomacy;

public interface IVersionedDiplomacy
{
	public Boolean getAllowToLand();
	public Integer getTurn();
	public String getForeignPolicy();
	public String getCible();
	public String getName();
}
