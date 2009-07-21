/**
 * @author Escallier Pierre
 * @file CelestialBody.java
 * @date 29 mai 2009
 */
package server.model;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import server.SEPServer;

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
	private final int startingCarbonStock;
	private final int slots;
	
	// Variables
	private int carbon;
	private Player owner;
	private int carbonStock;
	private final Map<Class<? extends ABuilding>, ABuilding> buildings;
	private final Map<String, Fleet> unasignedFleets;	
	private int lastBuildDate = -1;
	
	// Views
	private PlayerDatedView<Integer> playersLastObservation = new PlayerDatedView<Integer>();
	private PlayerDatedView<Integer> playersCarbonStockView = new PlayerDatedView<Integer>();
	private PlayerDatedView<Integer> playersCarbonView = new PlayerDatedView<Integer>();
	private PlayerDatedView<Player> playersOwnerView = new PlayerDatedView<Player>();
	private PlayerDatedView<HashSet<common.IBuilding>> playersBuildingsView = new PlayerDatedView<HashSet<common.IBuilding>>();
	private Map<String, PlayerDatedView<common.Fleet>> playersUnasignedFleetsView = new HashMap<String, PlayerDatedView<common.Fleet>>();
	
	// Turn resolution variables
	private final Stack<String> conflictInitiators = new Stack<String>();
	
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
	public ProductiveCelestialBody(String name, int startingCarbonStock, int slots, Player owner)
	{
		this.name = name;
		this.startingCarbonStock = startingCarbonStock;
		this.carbonStock = this.startingCarbonStock;
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
		this.startingCarbonStock = random.nextInt(carbonAmount[1] - carbonAmount[0]) + carbonAmount[0];
		this.carbonStock = this.startingCarbonStock;
		
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
	
	public int getStartingCarbonStock()
	{
		return startingCarbonStock;
	}
	
	protected int getCarbonStockView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			playersCarbonStockView.updateView(playerLogin, carbonStock, date);
		}
		
		return playersCarbonStockView.getLastValue(playerLogin, -1);
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
	
	protected Map<common.StarshipTemplate, Integer> getUnasignedFleetStarships(String playerLogin)
	{
		if (!unasignedFleets.containsKey(playerLogin))
		{
			return null;
		}
		
		return unasignedFleets.get(playerLogin).getStarships();
	}
	
	protected Set<common.ISpecialUnit> getUnasignedFleetSpecialUnits(String playerLogin)
	{
		if (!unasignedFleets.containsKey(playerLogin))
		{
			return null;
		}
		
		return unasignedFleets.get(playerLogin).getSpecialUnits();
	}
	
	public void mergeToUnasignedFleet(Player player, Map<common.StarshipTemplate, Integer> starshipsToMake, Set<common.ISpecialUnit> specialUnitsToMake)
	{
		if (!unasignedFleets.containsKey(player.getName()))
		{
			unasignedFleets.put(player.getName(), new Fleet("Unasigned fleet", player, null, starshipsToMake, specialUnitsToMake, true));
		}
		else
		{
			unasignedFleets.get(player.getName()).merge(starshipsToMake, specialUnitsToMake);
		}		
	}	
	
	public void removeFromUnasignedFleet(Player player, Map<common.StarshipTemplate, Integer> fleetToForm, Set<common.ISpecialUnit> specialUnitsToForm)
	{
		if (unasignedFleets.get(player.getName()) == null) throw new Error("Tried to remove starships from an empty unasigned fleet.");
		
		unasignedFleets.get(player.getName()).remove(fleetToForm, specialUnitsToForm);
	}
	
	public Fleet getUnasignedFleet(String playerLogin)
	{
		return unasignedFleets.get(playerLogin);
	}
	
	public <B extends ABuilding> B getBuilding(Class<B> buildingType)
	{
		if (buildings.containsKey(buildingType))
		{
			return buildingType.cast(buildings.get(buildingType));
		}
		
		return null;
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
	
	static int getNaturalCarbonPerTurn(Class<? extends ProductiveCelestialBody> productiveCelestialBodyType)
	{
		// TODO : Distinguish between productive celestial body type.		
		return common.ProductiveCelestialBody.NATURAL_CARBON_PER_TURN;
	}
	
	static int getMaxNaturalCarbon(Class<? extends ProductiveCelestialBody> productiveCelestialBodyType)
	{
		// TODO : Distinguish between productive celestial body type.		
		return common.ProductiveCelestialBody.MAX_NATURAL_CARBON;
	}

	public void decreaseCarbonStock(int generatedCarbon)
	{		
		this.carbonStock -= generatedCarbon;
	}
	
	public String getOwnerName()
	{
		return owner == null ? null : owner.getName();
	}

	public void addConflictInititor(String initiatorLogin)
	{
		conflictInitiators.add(initiatorLogin);
	}
	
	public Stack<String> getConflictInitiators()
	{
		return conflictInitiators;
	}

	public Fleet getDefenseFleet()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
