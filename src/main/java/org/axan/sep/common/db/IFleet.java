package org.axan.sep.common.db;

import java.util.List;
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
	
	/**
	 * Return ordered list of planned moves.
	 * @return
	 */
	List<FleetMove> getMoves();
	
	/**
	 * Set new moves plan.
	 * @param newPlan
	 */
	void updateMovesPlan(List<FleetMove> newPlan);
	
	/**
	 * If fleet is stopped, check for next destination in moves plan and set new destination and/or decrement delay.
	 * @return true if fleet is on the run ({@link #isStopped()} == false) after method call, even if no change are made.
	 */
	boolean nextDestination();
	
	@Override
	public IFleetMarker getMarker(double step);
}
