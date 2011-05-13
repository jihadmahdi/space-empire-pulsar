package org.axan.sep.common.db;

import org.axan.sep.common.Protocol.eBuildingType;

public interface IBuilding
{
	public eBuildingType getType();
	public String getCelestialBodyName();
	public Integer getTurn();
	public Integer getNbSlots();
}
