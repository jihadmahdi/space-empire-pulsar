/**
 * @author Escallier Pierre
 * @file CelestialBody.java
 * @date 29 mai 2009
 */
package server.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import common.GameConfig;
import common.Player;



/**
 * Abstract class that represent a celestial body.
 */
abstract class ProductiveCelestialBody implements ICelestialBody, Serializable
{
	protected static final Random random = new Random();
	
	private static final long	serialVersionUID	= 1L;
	
	// Constants
	private final String name;
	private final int carbonStock;
	private final int slots;
	
	// Variables
	private int carbon;
	private Player owner;
	private final Set<IBuilding> buildings;
	
	private int lastBuildDate = -1;
	
	// Views
	private PlayerDatedView<Integer> playersLastObservation = new PlayerDatedView<Integer>();
	private PlayerDatedView<Integer> playersCarbonView = new PlayerDatedView<Integer>();
	private PlayerDatedView<Player> playersOwnerView = new PlayerDatedView<Player>();
	private PlayerDatedView<HashSet<common.IBuilding>> playersBuildingsView = new PlayerDatedView<HashSet<common.IBuilding>>();
	
	public static class CelestialBodyBuildException extends Exception
	{
		private static final long	serialVersionUID	= 1L;

		public CelestialBodyBuildException(String msg)
		{
			super(msg);
		}
	}
	
	/**
	 * Full constructor.
	 */
	public ProductiveCelestialBody(String name, int carbonStock, int slots, Player owner)
	{
		this.name = name;
		this.carbonStock = carbonStock;
		this.slots = slots;
		this.owner = owner;
		this.buildings = new HashSet<IBuilding>();
	}
	
	/**
	 * @param gameConfig
	 */
	public ProductiveCelestialBody(String name, GameConfig gameConfig, Class<? extends common.ICelestialBody> celestialBodyType)
	{
		this.name = name;
		
		// Fix carbon amount to the mean value.
		Integer[] carbonAmount = gameConfig.getCelestialBodiesStartingCarbonAmount().get(celestialBodyType);
		this.carbonStock = random.nextInt(carbonAmount[1] - carbonAmount[0]) + carbonAmount[0];
		
		// Fix slots amount to the mean value.
		Integer[] slotsAmount = gameConfig.getCelestialBodiesSlotsAmount().get(celestialBodyType);
		int slots = random.nextInt(slotsAmount[1] - slotsAmount[0]) + slotsAmount[0];
		if (slots <= 0) slots = 1;
		this.slots = slots;
		
		this.buildings = new HashSet<IBuilding>();
		
		this.owner = null;
	}
	
	public int getLastBuildDate()
	{
		return lastBuildDate;
	}
	
	public void setLastBuildDate(int date)
	{
		lastBuildDate = date;
	}
	
	public void updateBuilding(IBuilding building) throws CelestialBodyBuildException
	{
		IBuilding oldBuilding = null;
		
		int buildSlotsCount = 0;
		if (buildings != null) for (IBuilding b : buildings)
		{
			if (b.getClass().equals(building.getClass()))
			{
				buildSlotsCount += building.getBuildSlotsCount();
				oldBuilding = b;
			}
			else
			{
				buildSlotsCount += b.getBuildSlotsCount();
			}
		}
		
		if (oldBuilding == null)
		{
			buildSlotsCount += building.getBuildSlotsCount(); 
		}
		
		if (buildSlotsCount > slots) throw new CelestialBodyBuildException("Not enough free slots");
		
		if (oldBuilding != null) buildings.remove(oldBuilding);
		buildings.add(building);
	}
	
	/* (non-Javadoc)
	 * @see server.model.ICelestialBody#getOwner()
	 */
	@Override
	public Player getOwner()
	{
		return owner;
	}
	
	protected Player getOwnerView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			playersOwnerView.updateView(playerLogin, owner, date);
		}
		
		return playersOwnerView.getLastValue(playerLogin, null);
	}
	
	protected int getLastObservation(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			playersLastObservation.updateView(playerLogin, date, date);
		}
		
		return playersLastObservation.getLastValue(playerLogin, -1);
	}
	
	public String getName()
	{
		return name;
	}

	public int getCarbonStock()
	{
		return carbonStock;
	}
	
	protected int getCarbonView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			playersCarbonView.updateView(playerLogin, carbon, date);
		}
		
		return playersCarbonView.getLastValue(playerLogin, -1);
	}
	
	public int getSlots()
	{
		return slots;
	}
	
	protected Set<common.IBuilding> getBuildingsView(int date, String playerLogin, boolean isVisible)
	{
		HashSet<common.IBuilding> buildingsView;
		
		if (isVisible)
		{
			buildingsView = new HashSet<common.IBuilding>();
			for(IBuilding b : buildings)
			{
				buildingsView.add(b.getPlayerView(date, playerLogin));
			}
			playersBuildingsView.updateView(playerLogin, buildingsView, date);
		}
		else
		{
			buildingsView = playersBuildingsView.getLastValue(playerLogin, null);
		}		
		
		return buildingsView;
	}
	
	public Set<IBuilding> getBuildings()
	{
		return buildings;
	}
	
	public void setCarbon(int carbon)
	{
		this.carbon = carbon;
	}
	
	public int getCarbon()
	{
		return carbon;
	}
	
	public int getBuildSlotsCount()
	{
		int i = 0;
		if (buildings != null) for (IBuilding b : buildings)
		{
			i += b.getBuildSlotsCount();
		}
		return i;
	}
	
	public int getFreeSlotsCount()
	{
		return slots - getBuildSlotsCount();
	}
	
	abstract public boolean canBuild(IBuilding building);
}
