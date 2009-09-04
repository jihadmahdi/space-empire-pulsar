/**
 * @author Escallier Pierre
 * @file AsteroidField.java
 * @date 1 juin 2009
 */
package server.model;

import java.io.Serializable;
import java.util.Map;

import common.GameConfig;
import common.ICelestialBody;
import common.Player;
import common.SEPUtils.Location;

/**
 * This class represent an asteroid field.
 */
class AsteroidField extends ProductiveCelestialBody implements Serializable
{	
	private static final long	serialVersionUID	= 1L;

	public AsteroidField(DataBase db, String name, Location location, GameConfig gameConfig)
	{
		super(db, name, location, gameConfig, common.AsteroidField.class);
	}

	/**
	 * Full constructor.
	 */
	public AsteroidField(DataBase db, String name, Location location, int carbonStock, int slots, String ownerName)
	{
		super(db, name, location, carbonStock, slots, ownerName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see common.CelestialBody#canBuild(server.model.Building)
	 */
	@Override
	public boolean canBuild(ABuilding building)
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
		return new common.AsteroidField(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getStartingCarbonStock(), getCarbonStockView(date, playerLogin, isVisible), getCarbonView(date, playerLogin, isVisible), getSlots(), getBuildingsView(date, playerLogin, isVisible), getOwnerNameView(date, playerLogin, isVisible), getUnasignedFleetStarships(playerLogin), getUnasignedFleetSpecialUnits(playerLogin));
	}	

}
