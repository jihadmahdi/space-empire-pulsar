package org.axan.sep.common.db;

import org.axan.sep.common.Protocol.eBuildingType;
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
	
	void payCarbon(int carbonCost);
	void extractCarbon(int extractedCarbon);
	IFleet getAssignedFleet(String playerName);
	IBuilding getBuilding(eBuildingType type);
}
