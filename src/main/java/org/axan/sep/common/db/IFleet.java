package org.axan.sep.common.db;

import java.util.Map;

import org.axan.sep.common.Rules.StarshipTemplate;

public interface IFleet extends IFleetMarker, IUnit
{		
	/**
	 * Add starships to current fleet.
	 * @param newcomers
	 */
	void addStarships(Map<StarshipTemplate, Integer> newcomers);
	
	/**
	 * Remove starships from current fleet.
	 * @param leavers
	 */
	void removeStarships(Map<StarshipTemplate, Integer> leavers);	
}
