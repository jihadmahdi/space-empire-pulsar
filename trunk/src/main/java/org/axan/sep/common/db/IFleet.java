package org.axan.sep.common.db;

import java.util.Map;

import org.axan.sep.common.Rules.StarshipTemplate;

public interface IFleet extends IUnit
{
	Map<StarshipTemplate, Integer> getStarships();
	void addStarships(Map<StarshipTemplate, Integer> newcomers);
	void removeStarships(Map<StarshipTemplate, Integer> leavers);
	// boolean isEmpty(); No need, empty fleet should automatically be destroyed.	
}
