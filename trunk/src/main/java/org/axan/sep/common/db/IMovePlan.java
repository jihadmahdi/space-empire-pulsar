package org.axan.sep.common.db;

import java.util.Map;

public interface IMovePlan
{
	public String getOwner();
	public String getName();
	public Integer getPriority();
	public Integer getDelay();
	public Boolean getAttack();
	public String getDestination();
	public Map<String, Object> getNode();
}
