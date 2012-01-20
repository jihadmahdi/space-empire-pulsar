package org.axan.sep.common.db;

import org.neo4j.graphdb.Node;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.db.IGameConfig;

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
}
