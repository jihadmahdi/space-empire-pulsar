/**
 * @author Escallier Pierre
 * @file CelestialBody.java
 * @date 29 mai 2009
 */
package common;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import server.model.Building;
import server.model.Planet;

/**
 * Abstract class that represent a celestial body.
 */
public abstract class CelestialBody
{
	protected static final Random random = new Random();
	
	protected final int carbonStock;
	protected final int slots;
	protected Player owner;
	protected final Set<Building> buildings;
	
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
	public CelestialBody(int carbonStock, int slots, Player owner)
	{
		this.carbonStock = carbonStock;
		this.slots = slots;
		this.owner = owner;
		this.buildings = new HashSet<Building>();
	}
	
	/**
	 * @param gameConfig
	 */
	public CelestialBody(GameConfig gameConfig)
	{
		// Fix carbon amount to the mean value.
		Integer[] carbonAmount = gameConfig.getCelestialBodiesStartingCarbonAmount().get(getClass());
		this.carbonStock = random.nextInt(carbonAmount[1] - carbonAmount[0]) + carbonAmount[0];
		
		// Fix slots amount to the mean value.
		Integer[] slotsAmount = gameConfig.getCelestialBodiesSlotsAmount().get(getClass());
		int slots = random.nextInt(slotsAmount[1] - slotsAmount[0]) + slotsAmount[0];
		if (slots <= 0) slots = 1;
		this.slots = slots;
		
		this.buildings = new HashSet<Building>();
		
		this.owner = null;
	}

	/**
	 * Build the given building on the current celestial body.
	 * Check for free slot to build, abstract method canBuild(Building)id called on the final instance to check if this building can be build on this celestial body.
	 * @param building
	 * @throws CelestialBodyBuildException
	 */
	public void build(Building building) throws CelestialBodyBuildException
	{
		if (buildings.size() >= slots)
		{
			throw new CelestialBodyBuildException("No more free slot.");
		}
		
		if (!canBuild(building))
		{
			throw new CelestialBodyBuildException("Cannot build.");
		}
		
		buildings.add(building);
	}
	
	abstract public boolean canBuild(Building building);
}
