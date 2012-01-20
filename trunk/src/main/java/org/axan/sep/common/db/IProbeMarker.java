package org.axan.sep.common.db;

public interface IProbeMarker extends IUnitMarker
{
	/**
	 * Return true if current probe is deployed.
	 */
	boolean isDeployed();
	
	/**
	 * probe serie name (part of unit name).
	 */
	String getSerieName();
	
	/**
	 * probe serial number (part of unit name).
	 * @return
	 */
	int getSerialNumber();
}
