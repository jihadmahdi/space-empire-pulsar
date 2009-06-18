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

import client.gui.RunningGamePanel;

import common.IStarship;
import common.Protocol;
import common.Protocol.ServerRunningGame.RunningGameCommandException;

/**
 * 
 */
class StarshipPlant implements IBuilding, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final int lastBuildDate;
	
	private final Map<Class<? extends common.IStarship>, Integer> starships = new Hashtable<Class<? extends common.IStarship>, Integer>();
	
	public StarshipPlant(int lastBuildDate)
	{
		this.lastBuildDate = lastBuildDate;
	}
	
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

	@Override
	public int getLastBuildDate()
	{
		return lastBuildDate;
	}

	public void makeStarships(Map<Class<? extends IStarship>, Integer> starshipsToMake)
	{
		for(Map.Entry<Class<? extends IStarship>, Integer> e : starshipsToMake.entrySet())
		{
			Class<? extends IStarship> starshipType = e.getKey();
			if (starships.containsKey(starshipType))
			{
				int currentQt = (starships.get(starshipType) == null)?0:starships.get(starshipType);
				currentQt += (e.getValue()==null?0:e.getValue());
				starships.put(starshipType, currentQt);
			}
			else if (e.getValue() != null && e.getValue() > 0)
			{
				starships.put(starshipType, e.getValue());
			}
		}
	}
	
	public void removeStarships(Map<Class<? extends IStarship>, Integer> starshipsToRemove) throws RunningGameCommandException
	{
		for(Map.Entry<Class<? extends IStarship>, Integer> e : starshipsToRemove.entrySet())
		{
			Class<? extends IStarship> starshipType = e.getKey();
			if (starships.containsKey(starshipType))
			{
				if (e.getValue() == null || e.getValue() <= 0) continue;
				
				int currentQt = (starships.get(starshipType) == null)?0:starships.get(starshipType);
				currentQt -= e.getValue();
				
				if (currentQt < 0) throw new RunningGameCommandException("Not enough starships for type '"+e.getKey().getSimpleName()+"'");
				
				starships.put(starshipType, currentQt);
			}
			else
			{
				throw new RunningGameCommandException("Cannot remove unexisting starships from starshipPlant.");
			}
		}
	}

	public Map<Class<? extends IStarship>, Integer> getStarships()
	{
		return starships;
	}
}
