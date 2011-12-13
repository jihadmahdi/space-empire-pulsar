package org.axan.sep.common.db;

import java.util.Map;
import org.axan.sep.common.Protocol.eSpecialUnitType;

public interface ISpecialUnit
{
	public String getOwner();
	public String getName();
	public eSpecialUnitType getType();
	public String getFleetName();
	public Map<String, Object> getNode();
}
