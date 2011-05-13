package org.axan.sep.common.db;


public interface IMovePlan
{
	public String getOwner();
	public String getName();
	public Integer getTurn();
	public Integer getPriority();
	public Integer getDelay();
	public Boolean getAttack();
	public String getDestination();
}
