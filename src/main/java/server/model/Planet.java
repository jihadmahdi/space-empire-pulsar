/**
 * @author Escallier Pierre
 * @file Planet.java
 * @date 1 juin 2009
 */
package server.model;

import java.util.Random;

import common.CelestialBody;
import common.GameConfig;
import common.Player;
import common.CelestialBody.CelestialBodyBuildException;

/**
 * This class represent a planet in the universe.
 */
public class Planet extends CelestialBody
{
	private static final Random random = new Random();
	
	public static final int	CARBON_MIN				= 50*1000;

	public static final int	CARBON_MAX				= 100*1000;
	
	public static final int SLOTS_MIN = 4;
	
	public static final int SLOTS_MAX = 10;

	public static final int	POPULATION_LIMIT_MIN			= 50*1000;

	public static final int	POPULATION_LIMIT_MAX			= 150*1000;

	public static final int	POPULATION_PER_TURN_MIN	= 2500;

	public static final int	POPULATION_PER_TURN_MAX	= 7500;

	public static final float GENERATION_RATE = (float) 0.30;
	
	private final int		populationPerTurn;

	private final int		populationLimit;

	/**
	 * Creation a new starting planet for the given player and game configuration.
	 * 
	 * @param player
	 *            Player to create the starting planet.
	 * @param gameConfig
	 *            Game config.
	 * @return Planet new starting planet.
	 */
	public static Planet newStartingPlanet(Player player, GameConfig gameConfig)
	{
		// Fix carbon amount to the mean value.
		Integer[] carbonAmount = gameConfig.getCelestialBodiesStartingCarbonAmount().get(Planet.class);
		int carbonStock = (carbonAmount[1] - carbonAmount[0]) / 2 + carbonAmount[0];

		// Fix slots amount to the mean value.
		Integer[] slotsAmount = gameConfig.getCelestialBodiesSlotsAmount().get(Planet.class);
		int slots = (slotsAmount[1] - slotsAmount[0]) / 2 + slotsAmount[0];
		if (slots <= 0) slots = 1;

		int[] populationPerTurnRange = gameConfig.getPopulationPerTurn();
		int populationPerTurn = (populationPerTurnRange[1] - populationPerTurnRange[0])/2 + populationPerTurnRange[0];
		
		int[] populationLimitRange = gameConfig.getPopulationLimit();
		int populationLimit = (populationLimitRange[1] - populationLimitRange[0])/2 + populationLimitRange[0];
		
		Planet planet = new Planet(carbonStock, slots, player, populationPerTurn, populationLimit);
		
		// If victory rule "Regimicide" is on, starting planet has a pre-built government module.
		if (gameConfig.isRegimicide())
		{
			GovernmentModule gov = new GovernmentModule(player);
			try
			{
				planet.build(gov);
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
	public Planet(GameConfig gameConfig)
	{
		super(gameConfig);
		
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
	private Planet(int carbonStock, int slots, Player owner, int populationPerTurn, int populationLimit)
	{
		super(carbonStock, slots, owner);
		this.populationPerTurn = populationPerTurn;
		this.populationLimit = populationLimit;
	}

	/* (non-Javadoc)
	 * @see common.CelestialBody#canBuild(server.model.Building)
	 */
	@Override
	public boolean canBuild(Building building)
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
}
