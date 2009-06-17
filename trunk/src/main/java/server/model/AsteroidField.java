/**
 * @author Escallier Pierre
 * @file AsteroidField.java
 * @date 1 juin 2009
 */
package server.model;

import java.io.Serializable;

import common.GameConfig;
import common.ICelestialBody;
import common.Player;

/**
 * This class represent an asteroid field.
 */
class AsteroidField extends ProductiveCelestialBody implements Serializable
{	
	private static final long	serialVersionUID	= 1L;

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

	/* (non-Javadoc)
	 * @see server.model.ICelestialBody#getPlayerView(int, java.lang.String, boolean)
	 */
	@Override
	public common.AsteroidField getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		return new common.AsteroidField(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getCarbonStock(), getCarbonView(date, playerLogin, isVisible), getSlots(), getBuildingsView(date, playerLogin, isVisible), getOwnerView(date, playerLogin, isVisible));
	}

}
