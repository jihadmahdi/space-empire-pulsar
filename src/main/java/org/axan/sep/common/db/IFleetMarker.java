package org.axan.sep.common.db;

import java.util.Map;

import org.axan.sep.common.Rules.StarshipTemplate;

public interface IFleetMarker extends IUnitMarker
{
	/**
	 * Return fleet composition (quantity for each starship template)
	 */
	Map<StarshipTemplate, Integer> getStarships();
}
