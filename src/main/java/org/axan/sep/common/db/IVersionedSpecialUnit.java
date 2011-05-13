package org.axan.sep.common.db;


public interface IVersionedSpecialUnit extends ISpecialUnit
{
	public Integer getTurn();
	public String getFleetOwner();
	public String getFleetName();
	public Integer getFleetTurn();
}
