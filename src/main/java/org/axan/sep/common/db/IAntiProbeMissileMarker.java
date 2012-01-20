package org.axan.sep.common.db;

public interface IAntiProbeMissileMarker extends IUnitMarker
{
	/**
	 * Return true if anti probe missile has already been fired.
	 * @return
	 */
	boolean isFired();
	
	/**
	 * anti probe missile serie name (part of unit name).
	 */
	String getSerieName();
	
	/**
	 * anti probe missile serial number (part of unit name).
	 * @return
	 */
	int getSerialNumber();
}
