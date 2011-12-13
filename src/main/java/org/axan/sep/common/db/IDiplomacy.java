package org.axan.sep.common.db;

import java.util.Map;

public interface IDiplomacy
{
	public String getOwner();
	public String getTarget();
	public Boolean getAllowToLand();
	public String getForeignPolicy();
	public Map<String, Object> getNode();
}
