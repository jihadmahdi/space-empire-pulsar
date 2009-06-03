/**
 * @author Escallier Pierre
 * @file FilteredPlanet.java
 * @date 3 juin 2009
 */
package common;

import java.util.Set;

/**
 * 
 */
public class Planet extends ProductiveCelestialBody
{
	public static final int	CARBON_MIN				= 50*1000;

	public static final int	CARBON_MAX				= 100*1000;
	
	public static final int SLOTS_MIN = 4;
	
	public static final int SLOTS_MAX = 10;

	public static final int	POPULATION_LIMIT_MIN			= 50*1000;

	public static final int	POPULATION_LIMIT_MAX			= 150*1000;

	public static final int	POPULATION_PER_TURN_MIN	= 2500;

	public static final int	POPULATION_PER_TURN_MAX	= 7500;

	public static final float GENERATION_RATE = (float) 0.30;
	
	// Constants
	private final int populationLimit;
	private final int populationPerTurn;
	
	// Only if visible
	private final int population;
	
	/**
	 * Full constructor.
	 */
	public Planet(boolean isVisible, int lastObservation, String name, int carbonStock, int carbon, int slots, Set<IBuilding> buildings, Player owner, int populationLimit, int populationPerTurn, int population)
	{
		super(isVisible, lastObservation, name, carbonStock, carbon, slots, buildings, owner);
		this.populationLimit = populationLimit;
		this.populationPerTurn = populationPerTurn;
		this.population = population;
	}
}
