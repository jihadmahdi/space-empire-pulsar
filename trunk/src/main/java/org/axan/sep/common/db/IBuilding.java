package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.Rules;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface IBuilding
{
	/**
	 * Return productive celestial body name.
	 */
	String getProductiveCelestialBodyName();
	
	/**
	 * Return building type.
	 */
	eBuildingType getType();
	
	/**
	 * Return last upgrade/build date.
	 */
	int getBuiltDate();
	
	/**
	 * Return how many slots this building occupy.
	 */
	int getNbSlots();
	
	/**
	 * Upgrade the building (++nbSlots). Must only be called if building can actually be upgraded.
	 * @see Rules#getBuildingCanBeUpgraded(eBuildingType)
	 */
	void upgrade();
	
	/**
	 * Downgrade the building (--nbSlots). Must only be called if building can actually be downgraded.
	 * @see Rules#getBuildingCanBeDowngraded(IBuilding)
	 */
	void downgrade();
	
	/**
	 * Demolish the building (remove from DB).
	 */
	void demolish();
	
	/**
	 * Update current building with given off-DB buildingUpdate.
	 * @param buildingUpdate must be the same type of the current building instance.
	 */
	void update(IBuilding buildingUpdate);
}
