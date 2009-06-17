/**
 * @author Escallier Pierre
 * @file StarshipPlant.java
 * @date 1 juin 2009
 */
package server.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

/**
 * 
 */
class StarshipPlant implements IBuilding, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final Map<Class<? extends common.IStarship>, Integer> starships = new Hashtable<Class<? extends common.IStarship>, Integer>();
	
	/* (non-Javadoc)
	 * @see server.model.IBuilding#getPlayerView(int, java.lang.String)
	 */
	@Override
	public common.StarshipPlant getPlayerView(int date, String playerLogin)
	{
		return new common.StarshipPlant(Collections.unmodifiableMap(starships));
	}

	@Override
	public int getBuildSlotsCount()
	{
		return 1;
	}
}
