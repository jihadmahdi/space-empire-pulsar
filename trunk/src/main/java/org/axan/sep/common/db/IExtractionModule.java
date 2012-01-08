package org.axan.sep.common.db;

import org.axan.sep.common.db.IBuilding;
import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface IExtractionModule extends IBuilding
{
	int getCarbonProductionPerTurn();
}
