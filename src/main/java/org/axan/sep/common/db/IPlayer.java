package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.IDiplomacyMarker.eForeignPolicy;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public interface IPlayer extends Serializable
{
	/**
	 * Order players by name.
	 */
	static class NameComparator implements Comparator<IPlayer>, Serializable
	{		
		@Override
		public int compare(IPlayer o1, IPlayer o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	}
	
	public static final NameComparator nameComparator = new NameComparator();
	
	/** Return player name. */
	String getName();
	
	/** Return player config. */
	IPlayerConfig getConfig();
	
	/** Return player units (including moving units). */
	Set<? extends IUnit> getUnits(eUnitType type);
	
	/** Return last versions of all units markers (including units) owned (unit owner) by current player (according to current DB view). */
	Set<? extends IUnitMarker> getUnitsMarkers(eUnitType type);

	/** Return last version of named unit marker (including units) owned (unit owner) by current player (according to current DB view). */
	IUnitMarker getUnitMarker(String name);

	/** Return modifiable IDiplomacy object toward given target. This only works on current player view. */
	IDiplomacy getDiplomacy(String targetName);
	
	/**
	 * Create or reset the player diplomacy toward target (on current db view).
	 * Must only be called on current player view, for other players @see {@link #setDiplomacyMarker(String, boolean, eForeignPolicy)} instead.
	 * @param targetName
	 * @param isAllowedToLand
	 * @param foreignPolicy
	 */
	void setDiplomacy(String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy);
	
	/** Return diplomacy marker of current player toward given target. Diplomacy markers are refreshed each time the player government is observed. */
	IDiplomacyMarker getDiplomacyMarker(String targetName);

	/** Return governmental modul of the curren player, or null if none. */
	IGovernmentModule getGovernmentModule();

	/**
	 * Create and set the player diplomacy marker toward target (on curren db view).
	 * Must not be called on current player view (@see {@link #setDiplomacy(String, boolean, eForeignPolicy)} instead).
	 * Must be called only one time per turn per target.
	 * @param targetName
	 * @param isAllowedToLand
	 * @param foreignPolicy
	 */
	void setDiplomacyMarker(String targetName, boolean isAllowedToLand, eForeignPolicy foreignPolicy);
}
