package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface IBuilding
{
	String getProductiveCelestialBodyName();
	eBuildingType getType();
	int getBuiltDate();
	int getNbSlots();
	
	void upgrade();
}
