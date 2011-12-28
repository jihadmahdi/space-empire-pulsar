package org.axan.sep.common.db;

import org.axan.sep.common.db.ICelestialBody;
import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface IVortex extends ICelestialBody
{
	public int getBirth();
	public int getDeath();
	// public String getDestination();
}
