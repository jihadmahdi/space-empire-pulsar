/**
 * @author Escallier Pierre
 * @file CelestialBody.java
 * @date 29 mai 2009
 */
package server.model;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import common.GameConfig;
import common.Player;



/**
 * Abstract class that represent a celestial body.
 */
abstract class ProductiveCelestialBody implements ICelestialBody
{
	protected static final Random random = new Random();
	
	// Constants
	protected final String name;
	protected final int carbonStock;
	protected final int slots;
	
	// Variables
	protected int carbon;
	protected Player owner;
	protected final Set<IBuilding> buildings;
	
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

	/**
	 * Build the given building on the current celestial body.
	 * Check for free slot to build, abstract method canBuild(Building)id called on the final instance to check if this building can be build on this celestial body.
	 * @param building
	 * @throws CelestialBodyBuildException
	 */
	public void build(IBuilding building) throws CelestialBodyBuildException
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
	
	/* (non-Javadoc)
	 * @see server.model.ICelestialBody#getOwner()
	 */
	@Override
	public Player getOwner()
	{
		return owner;
	}
	
	abstract public boolean canBuild(IBuilding building);
}
