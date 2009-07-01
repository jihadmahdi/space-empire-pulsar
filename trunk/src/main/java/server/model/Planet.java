/**
 * @author Escallier Pierre
 * @file Planet.java
 * @date 1 juin 2009
 */
package server.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import server.model.ProductiveCelestialBody.CelestialBodyBuildException;

import common.GameConfig;
import common.IStarship;
import common.Player;

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
	public static Planet newStartingPlanet(String name, Player player, GameConfig gameConfig)
	{
		// Fix carbon amount to the mean value.
		Integer[] carbonAmount = gameConfig.getCelestialBodiesStartingCarbonAmount().get(common.Planet.class);
		int carbonStock = (carbonAmount[1] - carbonAmount[0]) / 2 + carbonAmount[0];

		// Fix slots amount to the mean value.
		Integer[] slotsAmount = gameConfig.getCelestialBodiesSlotsAmount().get(common.Planet.class);
		int slots = (slotsAmount[1] - slotsAmount[0]) / 2 + slotsAmount[0];
		if (slots <= 0) slots = 1;

		int[] populationPerTurnRange = gameConfig.getPopulationPerTurn();
		int populationPerTurn = (populationPerTurnRange[1] - populationPerTurnRange[0])/2 + populationPerTurnRange[0];
		
		int[] populationLimitRange = gameConfig.getPopulationLimit();
		int populationLimit = (populationLimitRange[1] - populationLimitRange[0])/2 + populationLimitRange[0];
		
		Planet planet = new Planet(name, carbonStock, slots, player, populationPerTurn, populationLimit);
		
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
	public Planet(String name, GameConfig gameConfig)
	{
		super(name, gameConfig, common.Planet.class);
		
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
	private Planet(String name, int carbonStock, int slots, Player owner, int populationPerTurn, int populationLimit)
	{
		super(name, carbonStock, slots, owner);
		this.populationPerTurn = populationPerTurn;
		this.populationLimit = populationLimit;		
	}

	/* (non-Javadoc)
	 * @see server.model.ProductiveCelestialBody#canBuild(IBuilding)
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
	 * @see server.model.ICelestialBody#getPlayerView(int, java.lang.String, boolean)
	 */
	@Override
	public common.Planet getPlayerView(int date, String playerLogin, boolean isVisible)
	{		
		if (isVisible)
		{
			// Updates
			playersPopulationView.updateView(playerLogin, population, date);			
		}
		
		return new common.Planet(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getCarbonStock(), getCarbonView(date, playerLogin, isVisible), getSlots(), getBuildingsView(date, playerLogin, isVisible), getOwnerView(date, playerLogin, isVisible), getUnasignedFleetComposition(playerLogin), populationLimit, populationPerTurn, playersPopulationView.getLastValue(playerLogin, -1));
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
}
