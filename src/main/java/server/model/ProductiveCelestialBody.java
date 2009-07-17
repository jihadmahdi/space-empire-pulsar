/**
 * @author Escallier Pierre
 * @file CelestialBody.java
 * @date 29 mai 2009
 */
package server.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import common.GameConfig;
import common.IStarship;
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
	private final Map<Class<? extends ABuilding>, ABuilding> buildings;
	private final Map<String, Fleet> unasignedFleets;
	
	private int lastBuildDate = -1;
	
	// Views
	private PlayerDatedView<Integer> playersLastObservation = new PlayerDatedView<Integer>();
	private PlayerDatedView<Integer> playersCarbonView = new PlayerDatedView<Integer>();
	private PlayerDatedView<Player> playersOwnerView = new PlayerDatedView<Player>();
	private PlayerDatedView<HashSet<common.IBuilding>> playersBuildingsView = new PlayerDatedView<HashSet<common.IBuilding>>();
	private Map<String, PlayerDatedView<common.Fleet>> playersUnasignedFleetsView = new HashMap<String, PlayerDatedView<common.Fleet>>();
	
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
		this.buildings = new HashMap<Class<? extends ABuilding>, ABuilding>();
		this.unasignedFleets = new HashMap<String, Fleet>();
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
		
		this.buildings = new HashMap<Class<? extends ABuilding>, ABuilding>();
		this.unasignedFleets = new HashMap<String, Fleet>();
		
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
	
	public void updateBuilding(ABuilding building) throws CelestialBodyBuildException
	{
		ABuilding oldBuilding = null;
		
		int buildSlotsCount = 0;
		
		if (buildings != null) for(Map.Entry<Class<? extends ABuilding>, ABuilding> e : buildings.entrySet())
		{
			if (e.getKey().equals(building.getClass()))
			{
				buildSlotsCount += building.getBuildSlotsCount();
				oldBuilding = e.getValue();
			}
			else if (e.getValue() != null)
			{
				buildSlotsCount += e.getValue().getBuildSlotsCount();
			}
		}
		
		if (oldBuilding == null)
		{
			buildSlotsCount += building.getBuildSlotsCount(); 
		}
		
		if (buildSlotsCount > slots) throw new CelestialBodyBuildException("Not enough free slots");
		
		buildings.put(building.getClass(), building);
	}
	
	public void demolishBuilding(ABuilding existingBuilding)
	{
		ABuilding downgradedBuilding = existingBuilding.getDowngraded();
		if (downgradedBuilding == null)
		{
			buildings.remove(existingBuilding.getClass());
		}
		else
		{
			buildings.put(downgradedBuilding.getClass(), downgradedBuilding);
		}
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
			
			for(ABuilding b : buildings.values())
			{
				if (b == null) continue;
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
	
	protected Map<String, common.Fleet> getUnasignedFleetView(int date, String playerLogin, boolean isVisible)
	{
		Map<String, common.Fleet> result = new HashMap<String, common.Fleet>();
		
		if (isVisible)
		{
			for(String player : unasignedFleets.keySet())
			{
				if ((!playersUnasignedFleetsView.containsKey(player)) || playersUnasignedFleetsView.get(player) == null)
				{
					playersUnasignedFleetsView.put(player, new PlayerDatedView<common.Fleet>());
				}
				
				playersUnasignedFleetsView.get(player).updateView(playerLogin, unasignedFleets.get(player).getPlayerView(date, playerLogin, isVisible), date);
			}			
		}
		
		for(String player : playersUnasignedFleetsView.keySet())
		{
			common.Fleet unasignedFleetView = playersUnasignedFleetsView.get(player).getLastValue(playerLogin, null);
			if (unasignedFleetView != null && !unasignedFleetView.isEmpty())
			{
				result.put(player, unasignedFleetView);
			}
		}
		
		return result;
	}
	
	protected Map<Class<? extends IStarship>, Integer> getUnasignedFleetComposition(String playerLogin)
	{
		if (!unasignedFleets.containsKey(playerLogin))
		{
			return null;
		}
		
		return unasignedFleets.get(playerLogin).getComposition();
	}
	
	public void mergeToUnasignedFleet(Player player, Map<Class<? extends IStarship>, Integer> starshipsToMake)
	{
		if (!unasignedFleets.containsKey(player.getName()))
		{
			unasignedFleets.put(player.getName(), new Fleet("Unasigned fleet", player, null, starshipsToMake, true));
		}
		else
		{
			unasignedFleets.get(player.getName()).merge(starshipsToMake);
		}		
	}	
	
	public void removeFromUnasignedFleet(Player player, Map<Class<? extends IStarship>, Integer> fleetToForm)
	{
		if (unasignedFleets.get(player.getName()) == null) throw new Error("Tried to remove starships from an empty unasigned fleet.");
		
		unasignedFleets.get(player.getName()).remove(fleetToForm);
	}
	
	public Fleet getUnasignedFleet(String playerLogin)
	{
		return unasignedFleets.get(playerLogin);
	}
	
	public Collection<ABuilding> getBuildings()
	{
		return buildings.values();
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
		if (buildings != null) for (ABuilding b : buildings.values())
		{
			if (b == null) continue;
			
			i += b.getBuildSlotsCount();
		}
		return i;
	}
	
	public int getFreeSlotsCount()
	{
		return slots - getBuildSlotsCount();
	}
	
	public void removeBuilding(Class<? extends ABuilding> buildingType)
	{
		buildings.remove(buildingType);
	}
	
	abstract public boolean canBuild(ABuilding building);		
}
