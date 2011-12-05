package org.axan.sep.common.db;


public interface IProductiveCelestialBody extends ICelestialBody
{
	public Integer getInitialCarbonStock();
	public Integer getMaxSlots();
	public String getOwner();
	public Integer getCarbonStock();
	public Integer getCurrentCarbon();
}
