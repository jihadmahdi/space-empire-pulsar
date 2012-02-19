package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IGameEvent.IGameEventExecutor;

import java.util.Map;
import java.util.HashMap;

public interface IUnit extends IUnitMarker
{
	/**
	 * Traveling progress, between [0 and 1[
	 * Always return 0 if unit is stopped.
	 */
	double getTravelingProgress();
	
	/**
	 * Set traveling progress.
	 * Values must be between [0 and 1[
	 * @param travelingProgress
	 */
	void setTravelingProgress(double travelingProgress);
	
	/**
	 * Return departure celestial body name if any (null if none).
	 */
	String getDepartureName();
	
	/**
	 * Return departure location (cannot be null).
	 * If the unit is not traveling, then the departure is the current location.
	 */
	Location getDeparture();
	
	/**
	 * Set departure.
	 */
	void setDeparture(Location departure);
	
	/**
	 * Method called when unit finish its travel.
	 * Unit is not guaranteed to be able to land yet.
	 * e.g. AntiProbeMissiles check for target and explode; Fleets declare attack; 
	 */
	void onArrival(IGameEventExecutor executor);
	
	/**
	 * Return name of the departure celestial body (cannot be null).
	 * @see #getDeparture()
	 */
	String getInitialDepartureName();
	
	/**
	 * Return destination celestial body name or null if no destination or destination has no celestial body (e.g. probe destination area).
	 */
	String getDestinationName();
	
	/**
	 * Return destination location, or null if none.
	 */
	Location getDestination();
	
	/**
	 * Set the destination location.
	 * @param destination
	 */
	void setDestination(Location destination);
	
	/**
	 * Unit sight is the distance (area) to which it can see other units.
	 * TODO: distinguish between traveling sight and stopped sight.
	 */
	float getSight();
	
	/**
	 * Log unit encounter in this unit log.
	 * Logs are published when unit stops safely.
	 * @param encounteredUnitMarker
	 */
	void logEncounter(IUnitMarker encounteredUnitMarker);
	
	/**
	 * Get a marker of the current unit.
	 * @return
	 */
	IUnitMarker getMarker(double step);
	
	/**
	 * Destroy unit from current DB version.
	 */
	void destroy();
}
