/**
 * @author Escallier Pierre
 * @file AsteroidField.java
 * @date 1 juin 2009
 */
package server.model;

import common.GameConfig;
import common.Player;

/**
 * This class represent an asteroid field.
 */
class AsteroidField extends ProductiveCelestialBody
{	

	public AsteroidField(String name, GameConfig gameConfig)
	{
		super(name, gameConfig, common.AsteroidField.class);
	}

	/**
	 * Full constructor.
	 */
	public AsteroidField(String name, int carbonStock, int slots, Player owner)
	{
		super(name, carbonStock, slots, owner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see common.CelestialBody#canBuild(server.model.Building)
	 */
	@Override
	public boolean canBuild(IBuilding building)
	{
		// TODO
		return false;
	}

}
