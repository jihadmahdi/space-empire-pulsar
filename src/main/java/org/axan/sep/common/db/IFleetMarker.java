package org.axan.sep.common.db;

import java.util.List;
import java.util.Map;

import org.axan.sep.common.Rules.StarshipTemplate;

public interface IFleetMarker extends IUnitMarker
{
	/**
	 * Return fleet composition (quantity for each starship template)
	 */
	Map<StarshipTemplate, Integer> getStarships();
	
	/**
	 * Return true if fleet is a celestial body default assigned fleet (not named by the player).
	 * Such a fleet cannot be moved before a proper fleet is formed (with a name given by player).
	 * @return
	 */
	boolean isAssignedFleet();
}
