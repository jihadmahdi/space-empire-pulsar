package org.axan.sep.server.model.orm;

import org.axan.sep.common.Protocol.eBuildingType;

public interface IBuilding
{
	public Integer getNbSlots();
	public String getCelestialBodyName();
	public Integer getTurn();
	public eBuildingType getType();
}
