/**
 * @author Escallier Pierre
 * @file Planet.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;
import java.util.Random;

import org.axan.sep.common.GameConfig;
import org.axan.sep.common.SEPUtils.Location;



/**
 * This class represent a planet in the universe.
 */
class Planet extends ProductiveCelestialBody implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private static final Random random = new Random();
	
	// Constants
	private final int		populationLimit;
	private final int		populationPerTurn;
	
	// Variables
	private int population;
	
	// Views
	PlayerDatedView<Integer> playersPopulationView = new PlayerDatedView<Integer>();

	/**
	 * Creation a new starting planet for the given player and game configuration.
	 * 
	 * @param player
	 *            Player to create the starting planet.
	 * @param gameConfig
	 *            Game config.
	 * @return Planet new starting planet.
	 */
	public static Planet newStartingPlanet(DataBase db, String name, Location location, String playerName, GameConfig gameConfig)
	{
		// Fix carbon amount to the mean value.
		Integer[] carbonAmount = gameConfig.getCelestialBodiesStartingCarbonAmount().get(org.axan.sep.common.Planet.class);
		int carbonStock = (carbonAmount[1] - carbonAmount[0]) / 2 + carbonAmount[0];

		// Fix slots amount to the mean value.
		Integer[] slotsAmount = gameConfig.getCelestialBodiesSlotsAmount().get(org.axan.sep.common.Planet.class);
		int slots = (slotsAmount[1] - slotsAmount[0]) / 2 + slotsAmount[0];
		if (slots <= 0) slots = 1;

		int[] populationPerTurnRange = gameConfig.getPopulationPerTurn();
		int populationPerTurn = (populationPerTurnRange[1] - populationPerTurnRange[0])/2 + populationPerTurnRange[0];
		
		int[] populationLimitRange = gameConfig.getPopulationLimit();
		int populationLimit = (populationLimitRange[1] - populationLimitRange[0])/2 + populationLimitRange[0];
		
		Planet planet = new Planet(db, name, location, carbonStock, slots, playerName, populationPerTurn, populationLimit);
		
		planet.setCarbon(gameConfig.getPlayersPlanetsStartingCarbonResources());
		planet.population = gameConfig.getPlayersPlanetsStartingPopulation();
		
		// If victory rule "Regimicide" is on, starting planet has a pre-built government module.
		if (gameConfig.isRegimicide())
		{
			GovernmentModule gov = new GovernmentModule(-1);
			try
			{
				planet.updateBuilding(gov);
			}
			catch (CelestialBodyBuildException e)
			{
				throw new Error(e);
			}
		}
		
		return planet;
	}
	
	/**
	 * Neutral planet generation.
	 * @param gameCfg
	 */
	public Planet(DataBase db, String name, Location location, GameConfig gameConfig)
	{
		super(db, name, location, gameConfig, org.axan.sep.common.Planet.class);
		
		int[] populationPerTurnRange = gameConfig.getPopulationPerTurn();
		this.populationPerTurn = random.nextInt(populationPerTurnRange[1] - populationPerTurnRange[0]) + populationPerTurnRange[0];
		
		int[] populationLimitRange = gameConfig.getPopulationLimit();
		this.populationLimit = random.nextInt(populationLimitRange[1] - populationLimitRange[0]) + populationLimitRange[0];
	}

	/**
	 * Full constructor.
	 * 
	 * @param carbonStock
	 * @param slots
	 * @param populationPerTurn
	 * @param maxPopulation
	 */
	private Planet(DataBase db, String name, Location location, int carbonStock, int slots, String ownerName, int populationPerTurn, int populationLimit)
	{
		super(db, name, location, carbonStock, slots, ownerName);
		this.populationPerTurn = populationPerTurn;
		this.populationLimit = populationLimit;		
	}

	/* (non-Javadoc)
	 * @see org.axan.sep.server.model.ProductiveCelestialBody#canBuild(IBuilding)
	 */
	@Override
	public boolean canBuild(ABuilding building)
	{
		if (ExtractionModule.class.isInstance(building))
		{
			return true;
		}
		
		if (StarshipPlant.class.isInstance(building))
		{
			// TODO: Unicity control (planet).
			return true;
		}
		
		if (DefenseModule.class.isInstance(building))
		{
			return true;
		}
		
		if (GovernmentModule.class.isInstance(building))
		{
			// TODO: Check for global Player info, because Government is unique for each player.
			// TODO: Unicity control (player)
			return true;			
		}
		
		if (SpaceCounter.class.isInstance(building))
		{
			
		}
		
		// TODO: Implement other building types.
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.axan.sep.server.model.ICelestialBody#getPlayerView(int, java.lang.String, boolean)
	 */
	@Override
	public org.axan.sep.common.Planet getPlayerView(int date, String playerLogin, boolean isVisible)
	{		
		if (isVisible)
		{
			// Updates
			playersPopulationView.updateView(playerLogin, population, date);			
		}
		
		return new org.axan.sep.common.Planet(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getStartingCarbonStock(), getCarbonStockView(date, playerLogin, isVisible), getCarbonView(date, playerLogin, isVisible), getSlots(), getBuildingsView(date, playerLogin, isVisible), getOwnerNameView(date, playerLogin, isVisible), getUnasignedFleetStarshipViews(date, playerLogin, isVisible), getUnasignedFleetSpecialUnitViews(date, playerLogin, isVisible), populationLimit, populationPerTurn, playersPopulationView.getLastValue(playerLogin, -1));
	}
	
	public int getPopulation()
	{
		return population;
	}

	public boolean isGovernmentSettled()
	{
		for(ABuilding b : getBuildings())
		{
			if (GovernmentModule.class.isInstance(b)) return true;
		}
		
		return false;
	}

	public void setPopulation(int population)
	{
		this.population = population;
	}
	
	public int getPopulationPerTurn()
	{
		return populationPerTurn;
	}
	
	public int getPopulationLimit()
	{
		return populationLimit;
	}
}
