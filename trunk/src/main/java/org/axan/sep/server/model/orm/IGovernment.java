package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseGovernment;

public interface IGovernment
{
	public String getFleetName();
	public Integer getFleetTurn();
	public Integer getPlanetTurn();
	public String getOwner();
	public Integer getTurn();
	public String getPlanetName();
}
