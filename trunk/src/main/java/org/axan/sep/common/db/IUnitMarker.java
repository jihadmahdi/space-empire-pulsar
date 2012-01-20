package org.axan.sep.common.db;

import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.RealLocation;

public interface IUnitMarker
{
	/**
	 * Marker turn	
	 */
	int getTurn();
	
	/**
	 * Unit owner name (PK)
	 */
	String getOwnerName();
	
	/**
	 * Unit name (PK)
	 */
	String getName();
	
	/**
	 * Return true if unit is stopped, false if it is traveling (even if progress might be 0).
	 */
	boolean isStopped();
	
	/**
	 * Get current real location.
	 * It is equal to departure location if unit is stopped.
	 * @return
	 */
	RealLocation getRealLocation();
	
	/**
	 * Return unit type.
	 */
	eUnitType getType();
	
	/**
	 * Unit speed (area/turn)
	 */
	float getSpeed();
}
