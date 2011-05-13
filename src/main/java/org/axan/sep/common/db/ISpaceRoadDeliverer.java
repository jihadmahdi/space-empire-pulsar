package org.axan.sep.common.db;


public interface ISpaceRoadDeliverer extends IUnit
{
	public String getSourceType();
	public String getSourceCelestialBodyName();
	public Integer getSourceTurn();
	public String getDestinationType();
	public String getDestinationCelestialBodyName();
	public Integer getDestinationTurn();
}
