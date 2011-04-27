package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseVersionedSpecialUnit;

public interface IVersionedSpecialUnit
{
	public String getFleetName();
	public Integer getFleetTurn();
	public String getFleetOwner();
	public String getOwner();
	public Integer getTurn();
	public String getType();
	public String getName();
}
