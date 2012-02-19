package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface ICelestialBody
{
	/** Celestial body name */
	String getName();
	
	/** Celestial body location */
	Location getLocation();
	
	/** Celestial body type */
	eCelestialBodyType getType();
	
	/**
	 * Update current celestial body (including buildings, etc..) with given off-DB celestialBodyUpdate.
	 * @param celestialBodyUpdate must be the same type of the current celestial body instance.
	 */
	void update(ICelestialBody celestialBodyUpdate);
}
