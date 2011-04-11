/**
 * @author Escallier Pierre
 * @file CelestialBody.java
 * @date 29 mai 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.axan.sep.common.GameConfig;
import org.axan.sep.common.SEPUtils.Location;




/**
 * Abstract class that represent a celestial body.
 */
abstract class ProductiveCelestialBody implements ICelestialBody, Serializable
{
	protected static final Random random = new Random();
	
	private static final long	serialVersionUID	= 1L;
	
	// Primary Key
	private final ICelestialBody.Key key;
	
	// DB context
	protected final ISEPServerDataBase db;
	
	// Constants
	private final int startingCarbonStock;
	private final int slots;
	
	// Variables
	private int carbon;
	private String ownerName;
	private int carbonStock;
	private final Hashtable<Class<? extends ABuilding>, ABuilding> buildings;
	private final Hashtable<String, Fleet.Key> playersUnasignedFleetsKeys;
	private int lastBuildDate = -1;
	
	// Views
	private PlayerDatedView<Integer> playersLastObservation = new PlayerDatedView<Integer>();
	private PlayerDatedView<Integer> playersCarbonStockView = new PlayerDatedView<Integer>();
	private PlayerDatedView<Integer> playersCarbonView = new PlayerDatedView<Integer>();
	private PlayerDatedView<String> playersOwnerNameView = new PlayerDatedView<String>();
	private PlayerDatedView<HashSet<org.axan.sep.common.ABuilding>> playersBuildingsView = new PlayerDatedView<HashSet<org.axan.sep.common.ABuilding>>();
	private Map<String, PlayerDatedView<org.axan.sep.common.Fleet>> playersUnasignedFleetsView = new HashMap<String, PlayerDatedView<org.axan.sep.common.Fleet>>();
	
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
	public ProductiveCelestialBody(ISEPServerDataBase db, String name, Location location, int startingCarbonStock, int slots, String ownerName)
	{
		this.db = db;
		this.key = new ICelestialBody.Key(name, location);
		this.startingCarbonStock = startingCarbonStock;
		this.carbonStock = this.startingCarbonStock;
		this.slots = slots;
		this.ownerName = ownerName;
		this.buildings = new Hashtable<Class<? extends ABuilding>, ABuilding>();
		this.playersUnasignedFleetsKeys = new Hashtable<String, Fleet.Key>();
	}
	
	/**
	 * @param gameConfig
	 */
	public ProductiveCelestialBody(ISEPServerDataBase db, String name, Location location, GameConfig gameConfig, Class<? extends org.axan.sep.common.ICelestialBody> celestialBodyType)
	{
		this.db = db;
		this.key = new ICelestialBody.Key(name, location);
		
		// Fix carbon amount to the mean value.
		Integer[] carbonAmount = gameConfig.getCelestialBodiesStartingCarbonAmount().get(celestialBodyType);
		this.startingCarbonStock = random.nextInt(carbonAmount[1] - carbonAmount[0]) + carbonAmount[0];
		this.carbonStock = this.startingCarbonStock;
		
		// Fix slots amount to the mean value.
		Integer[] slotsAmount = gameConfig.getCelestialBodiesSlotsAmount().get(celestialBodyType);
		int slots = random.nextInt(slotsAmount[1] - slotsAmount[0]) + slotsAmount[0];
		if (slots <= 0) slots = 1;
		this.slots = slots;
		
		this.buildings = new Hashtable<Class<? extends ABuilding>, ABuilding>();
		this.playersUnasignedFleetsKeys = new Hashtable<String, Fleet.Key>();		
		this.ownerName = null;
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
	
	public <B extends ABuilding> void demolishBuilding(Class<B> buildingType)
	{
		buildings.remove(buildingType);
	}
	
	public void downgradeBuilding(ABuilding existingBuilding)
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
	 * @see org.axan.sep.server.model.ICelestialBody#getOwner()
	 */
	@Override
	public String getOwnerName()
	{
		return ownerName;
	}
	
	protected String getOwnerNameView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			playersOwnerNameView.updateView(playerLogin, ownerName, date);
		}
		
		return playersOwnerNameView.getLastValue(playerLogin, null);
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
		return key.getName();
	}
	
	public Location getLocation()
	{
		return key.getLocation();
	}
	
	@Override
	public ICelestialBody.Key getKey()
	{
		return key;
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
	
	protected Set<org.axan.sep.common.ABuilding> getBuildingsView(int date, String playerLogin, boolean isVisible)
	{
		HashSet<org.axan.sep.common.ABuilding> buildingsView;
		
		if (isVisible)
		{
			buildingsView = new HashSet<org.axan.sep.common.ABuilding>();
			
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
	
	protected Fleet getUnasignedFleet(String playerName)
	{
		if (!playersUnasignedFleetsKeys.containsKey(playerName)) return null;
		return db.getUnit(getLocation(), Fleet.class, playerName, playersUnasignedFleetsKeys.get(playerName).getName());
	}

	protected Map<String, org.axan.sep.common.Fleet> getUnasignedFleetsView(int date, String playerLogin, boolean isVisible)
	{
		Map<String, org.axan.sep.common.Fleet> result = new HashMap<String, org.axan.sep.common.Fleet>();
		
		if (isVisible)
		{
			for(String player : playersUnasignedFleetsKeys.keySet())
			{
				if ((!playersUnasignedFleetsView.containsKey(player)) || playersUnasignedFleetsView.get(player) == null)
				{
					playersUnasignedFleetsView.put(player, new PlayerDatedView<org.axan.sep.common.Fleet>());
				}							
				
				Fleet unasignedFleet = getUnasignedFleet(player);
				playersUnasignedFleetsView.get(player).updateView(playerLogin, (unasignedFleet == null) ? null : unasignedFleet.getPlayerView(date, playerLogin, isVisible), date);
			}			
		}
		
		for(String player : playersUnasignedFleetsView.keySet())
		{
			org.axan.sep.common.Fleet unasignedFleetView = playersUnasignedFleetsView.get(player).getLastValue(playerLogin, null);
			if (unasignedFleetView != null && !unasignedFleetView.isEmpty())
			{
				result.put(player, unasignedFleetView);
			}
		}
		
		return result;
	}
	
	/*
	protected Map<org.axan.sep.common.StarshipTemplate, Integer> getUnasignedFleetStarships(String playerName)
	{
		Fleet unasignedFleet = getUnasignedFleet(playerName);
		return unasignedFleet == null ? null : unasignedFleet.getStarships();
	}
	
	protected Map<org.axan.sep.common.StarshipTemplate, Integer> getUnasignedFleetStarshipViews(int date, String playerName, boolean isVisible)
	{
		Fleet unasignedFleet = getUnasignedFleet(playerName);
		return unasignedFleet == null ? null : unasignedFleet.getPlayerView(date, playerName, isVisible).getStarships();
	}
	
	
	protected Set<ISpecialUnit> getUnasignedFleetSpecialUnits(String playerName)
	{
		Fleet unasignedFleet = getUnasignedFleet(playerName);
		return unasignedFleet == null ? null : unasignedFleet.getSpecialUnits();		
	}
	
	protected Set<org.axan.sep.common.ISpecialUnit> getUnasignedFleetSpecialUnitViews(int date, String playerName, boolean isVisible)
	{
		Fleet unasignedFleet = getUnasignedFleet(playerName);
		return unasignedFleet == null ? null : unasignedFleet.getPlayerView(date, playerName, isVisible).getSpecialUnits();
	}
	*/
	
	public void mergeToUnasignedFleet(String playerName, Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMerge, Set<ISpecialUnit> specialUnitsToMerge)
	{
		Fleet unasignedFleet = getUnasignedFleet(playerName);
		
		if (unasignedFleet == null)
		{
			Fleet.Key key = new Fleet.Key(org.axan.sep.common.Fleet.getUnasignedFleetName(getName()), playerName);
			db.insertUnit(new Fleet(db, key, getLocation().asRealLocation(), starshipsToMerge, specialUnitsToMerge, true, null, null));
			playersUnasignedFleetsKeys.put(playerName, key);
		}
		else
		{
			unasignedFleet.merge(starshipsToMerge, specialUnitsToMerge);
		}			
	}	
	
	public void removeFromUnasignedFleet(String playerName, Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToRemove, Set<ISpecialUnit> specialUnitsToRemove)
	{
		Fleet unasignedFleet = getUnasignedFleet(playerName);
		if (unasignedFleet == null) throw new Error("Tried to remove starships from an empty unasigned fleet.");
		
		unasignedFleet.remove(fleetToRemove, specialUnitsToRemove);
	}
	
	public ABuilding getBuildingFromClientType(Class<? extends org.axan.sep.common.ABuilding> clientBuildingType)
	{
		return getBuilding(ABuilding.getServerBuildingClass(clientBuildingType));
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
		return org.axan.sep.common.ProductiveCelestialBody.NATURAL_CARBON_PER_TURN;
	}
	
	static int getMaxNaturalCarbon(Class<? extends ProductiveCelestialBody> productiveCelestialBodyType)
	{
		// TODO : Distinguish between productive celestial body type.		
		return org.axan.sep.common.ProductiveCelestialBody.MAX_NATURAL_CARBON;
	}

	public void decreaseCarbonStock(int generatedCarbon)
	{		
		this.carbonStock -= generatedCarbon;
	}
	
	public void addConflictInititor(String initiatorLogin)
	{
		conflictInitiators.add(initiatorLogin);
	}
	
	public Stack<String> getConflictInitiators()
	{
		return conflictInitiators;
	}

	public void changeOwner(String newOwnerName)
	{
		this.ownerName = newOwnerName;
	}

	public void endConflict()
	{
		conflictInitiators.clear();
	}

	public void controlNewcomer(Unit u)
	{
		if (conflictInitiators.contains(u.getOwnerName())) return; // Unit has already initiated a conflict.
		if (ownerName == null) return; // TODO: Neutral celestial bodies automatically defend themselves ? (game config option ?)
		if (ownerName.equals(u.getOwnerName())) return; // Unit is owned.		
		if (db.getPlayerPolicies(ownerName).getPolicies(u.getOwnerName()).isAllowedToLandFleetInHomeTerritory()) return; // Unit is allowed to land.
		addConflictInititor(ownerName); // Unit is not allowed to land, celestial body owner initiate the conflict.
	}
}
