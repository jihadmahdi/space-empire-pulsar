package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;

public interface IArea
{
	/** Area location. */
	Location getLocation();
	
	/** Return true if area is sun. */
	boolean isSun();
	
	// Connected api
	/** Return true if area is visible by given player (according to current DB view). */
	boolean isVisible(String playerName);
	
	/** Return celestial body or null if none. */
	ICelestialBody getCelestialBody();
	
	/** Return units in this area (stopped here, or currently travelling here). */
	Set<? extends IUnit> getUnits(eUnitType type);
	
	/** Return units markers (including units) in this area. */
	Set<? extends IUnitMarker> getUnitsMarkers(eUnitType type);

	/** Return readable string of this area for given player (according to current DB view). */
	String toString(String playerName);
}
