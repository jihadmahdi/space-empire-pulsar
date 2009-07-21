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
		return new common.StarshipPlant();
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
		return true;
	}
	
}
