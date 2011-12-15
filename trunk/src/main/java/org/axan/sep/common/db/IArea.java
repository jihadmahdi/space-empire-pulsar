package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;
import org.axan.sep.common.SEPUtils.Location;

public interface IArea
{
	public Location getLocation();
	boolean isSun();	
	boolean isVisible(SEPCommonDB db, String playerName) throws SQLDataBaseException;
	ICelestialBody getCelestialBody(SEPCommonDB db) throws SQLDataBaseException;
	<T extends IUnit> Set<T> getUnits(SEPCommonDB db, Class<T> expectedType) throws SQLDataBaseException;
	Map<String, Object> getNode();
}
