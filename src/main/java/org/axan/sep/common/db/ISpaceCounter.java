package org.axan.sep.common.db;

import org.axan.sep.common.db.IBuilding;
import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public interface ISpaceCounter extends IBuilding
{
	/**
	 * Return space roads built from the current space counter.
	 */
	Set<ISpaceRoad> getBuiltSpaceRoads();

	/**
	 * Return space roads linked to the current space counter.
	 */
	Set<ISpaceRoad> getLinkedSpaceRoads();

	/**
	 * Create a new space road from current space counter to productive celestial body given by destination name.
	 * Destination must already have a space counter.
	 * Current built space road count must be lesser than space counter slots.
	 * @param destinationName
	 */
	void buildSpaceRoad(String destinationName);
}
