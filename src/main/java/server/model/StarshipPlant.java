/**
 * @author Escallier Pierre
 * @file StarshipPlant.java
 * @date 1 juin 2009
 */
package server.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import server.model.SpaceCounter.SpaceRoad;

import client.gui.RunningGamePanel;

import common.GovernmentStarship;
import common.IStarship;
import common.Protocol;
import common.Protocol.ServerRunningGame.RunningGameCommandException;

/**
 * 
 */
class StarshipPlant extends ABuilding implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final int lastBuildDate;
	
	private final Map<Class<? extends common.IStarship>, Integer> starships = new Hashtable<Class<? extends common.IStarship>, Integer>();
	
	/**
	 * First build constructor.
	 */
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
	
	public void dismantleFleet(Fleet fleet)
	{
		Map<Class<? extends IStarship>, Integer> starships = fleet.getComposition();
		makeStarships(starships);
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

	@Override
	int getUpgradeCarbonCost()
	{
		return 0;
	}

	@Override
	int getUpgradePopulationCost()
	{
		return 0;
	}

	@Override
	ABuilding getUpgraded(int date)
	{
		throw new Error("Cannot upgrade Starship plant.");
	}
	
	@Override
	SpaceCounter getDowngraded()
	{
		return null;
	}

	@Override
	boolean canDowngrade()
	{
		// Starship plant can only be demolished if it is empty.
		
		if (starships != null) for(Map.Entry<Class<? extends IStarship>, Integer> e : starships.entrySet())
		{
			if (e.getValue() != null && e.getValue() > 0) return false;
		}
		
		return true;
	}
	
}
