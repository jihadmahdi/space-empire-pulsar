package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseFleetComposition;

public interface IFleetComposition
{
	public String getFleetName();
	public Integer getFleetTurn();
	public String getFleetOwner();
	public Integer getQuantity();
	public String getStarshipTemplate();
}
