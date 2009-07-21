/**
 * @author Escallier Pierre
 * @file FilteredPlanet.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class Planet extends ProductiveCelestialBody implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int	CARBON_MIN				= 50*1000;

	public static final int	CARBON_MAX				= 100*1000;
	
	public static final int SLOTS_MIN = 4;
	
	public static final int SLOTS_MAX = 10;

	public static final int	POPULATION_LIMIT_MIN			= 50*1000;

	public static final int	POPULATION_LIMIT_MAX			= 150*1000;

	public static final int	POPULATION_PER_TURN_MIN	= 2500;

	public static final int	POPULATION_PER_TURN_MAX	= 7500;

	public static final float GENERATION_RATE = (float) 0.30;

	public static final int PLAYERS_STARTING_CARBON = 2*1000;

	public static final int PLAYERS_STARTING_POPULATION = 50*1000;
	
	// Constants
	private final int populationLimit;
	private final int populationPerTurn;
	
	// Only if visible
	private final int population;
	
	/**
	 * Full constructor.
	 */
	public Planet(boolean isVisible, int lastObservation, String name, int startingCarbonStock, int carbonStock, int carbon, int slots, Set<IBuilding> buildings, Player owner, Map<StarshipTemplate, Integer> unasignedFleetStarships, Set<ISpecialUnit> unasignedFleetSpecialUnits, int populationLimit, int populationPerTurn, int population)
	{
		super(isVisible, lastObservation, name, startingCarbonStock, carbonStock, carbon, slots, buildings, owner, unasignedFleetStarships, unasignedFleetSpecialUnits);
		this.populationLimit = populationLimit;
		this.populationPerTurn = populationPerTurn;
		this.population = population;
	}

	/**
	 * @return population.
	 */
	public int getPopulation()
	{
		return population;
	}

	public int getPopulationLimit()
	{
		return populationLimit;
	}

	public int getPopulationPerTurn()
	{
		return populationPerTurn;
	}	

	/* (non-Javadoc)
	 * @see common.ProductiveCelestialBody#canBuildType(java.lang.Class)
	 */
	@Override
	public boolean canBuildType(Class<? extends IBuilding> buildingType)
	{
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append((getOwner() == null) ? "" : "[" + getOwner().getName() + "] ");
		sb.append(getName() + " (" + getClass().getSimpleName() + ")\n");
		sb.append("  Population : " + population + " / " + populationLimit+ "\n");
		sb.append("  Pop. per turn : "+populationPerTurn + "\n");
		sb.append("  Carbon : " + getCarbon() + " / " + getCarbonStock() + "\n");
		sb.append("  Slots : " + getBuildSlotsCount() + " / " + getSlots() + "\n");
		if (getBuildings() != null)
		{
			sb.append("  Buildings :\n");

			for (IBuilding b : getBuildings())
			{
				sb.append("    " + b.getClass().getSimpleName() + " : " + b.getBuildSlotsCount() + "\n");
			}
		}

		return sb.toString();
	}
}
