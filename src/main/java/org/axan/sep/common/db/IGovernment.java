package org.axan.sep.common.db;


public interface IGovernment
{
	public String getOwner();
	public Integer getTurn();
	public String getFleetName();
	public Integer getFleetTurn();
	public String getPlanetName();
	public Integer getPlanetTurn();
}
