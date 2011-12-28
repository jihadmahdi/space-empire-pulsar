package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.axan.sep.common.SEPUtils.Location;

public interface IArea
{
	Location getLocation();
	boolean isSun();
	
	// Connected api
	boolean isVisible(String playerName);
	ICelestialBody getCelestialBody();
	<T extends IUnit> Set<T> getUnits(Class<T> expectedType);
	String toString(String playerName);
}
