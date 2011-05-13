package org.axan.sep.common.db;


public interface IVersionedProductiveCelestialBody extends IProductiveCelestialBody
{
	public Integer getTurn();
	public String getOwner();
	public Integer getCarbonStock();
	public Integer getCurrentCarbon();
}
