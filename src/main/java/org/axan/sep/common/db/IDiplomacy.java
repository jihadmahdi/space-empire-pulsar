package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.db.IGameConfig;
import java.util.Map;
import java.util.HashMap;

public interface IDiplomacy extends IDiplomacyMarker
{
	/**
	 * Get a marker of the current diplomacy.
	 * @return
	 */
	IDiplomacyMarker getMarker();

	/**
	 * If true, target units are allowed to land on owner celestial bodies peacefully.
	 * Otherwise they are automatically attacked.
	 * @param isAllowedToLand
	 */
	void setAllowedToLand(boolean isAllowedToLand);
	
	/**
	 * Set foreign policy toward target player.
	 * @see {@link eForeignPolicy}
	 * @param foreignPolicy
	 */
	void setForeignPolicy(eForeignPolicy foreignPolicy);
}
