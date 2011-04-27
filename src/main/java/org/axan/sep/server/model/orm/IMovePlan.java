package org.axan.sep.server.model.orm;

import org.axan.sep.server.model.orm.base.IBaseMovePlan;

public interface IMovePlan
{
	public Integer getPriority();
	public String getOwner();
	public Boolean getAttack();
	public String getDestination();
	public Integer getTurn();
	public Integer getDelay();
	public String getName();
}
