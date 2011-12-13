package org.axan.sep.common.db;

import java.util.Map;
import org.axan.sep.common.Protocol.eBuildingType;

public interface IBuilding
{
	public eBuildingType getType();
	public String getCelestialBodyName();
	public Integer getTurn();
	public Integer getNbSlots();
	public Map<String, Object> getNode();
}
