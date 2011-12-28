package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface ICelestialBody
{
	String getName();
	Location getLocation();
	eCelestialBodyType getType();	
}
