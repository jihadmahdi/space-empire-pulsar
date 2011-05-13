package org.axan.sep.common.db;


public interface ICarbonCarrier extends IUnit
{
	public String getSourceType();
	public String getSourceCelestialBodyName();
	public Integer getSourceTurn();
}
