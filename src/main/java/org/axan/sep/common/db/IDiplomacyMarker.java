package org.axan.sep.common.db;

public interface IDiplomacyMarker
{
	/**
	 * Possibles foreign policies.
	 */
	public static enum eForeignPolicy
	{
		/** Do not attack first, only reply to attacks. */
		NEUTRAL,
		
		/** Always attack. */
		HOSTILE,
		
		/** Attack only if target player is owner of current celestial body (try to take it over). */
		HOSTILE_IF_OWNER;
	};
	
	/**
	 * Marker turn	
	 */
	int getTurn();
	
	/** Diplomacy owner. */
	public String getOwnerName();
	
	/** Diplomacy target. */
	public String getTargetName();
		
	/** Are target units allowed to land peacefully on owner celestial bodies. */
	public boolean isAllowedToLand();
	
	/** Get foreign policy toward current target. */
	public eForeignPolicy getForeignPolicy();
}
