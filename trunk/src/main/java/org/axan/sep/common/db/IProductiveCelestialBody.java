package org.axan.sep.common.db;

import org.axan.sep.common.db.ICelestialBody;
import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public interface IProductiveCelestialBody extends ICelestialBody
{
	int getInitialCarbonStock();
	int getMaxSlots();
	int getCarbonStock();
	int getCurrentCarbon();
	String getOwner();
	void setOwner(String ownerName);
	int getBuiltSlotsCount();
	Set<IBuilding> getBuildings();
}
