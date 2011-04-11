/**
 * @author Escallier Pierre
 * @file AsteroidField.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;

import org.axan.sep.common.GameConfig;
import org.axan.sep.common.SEPUtils.Location;



/**
 * This class represent an asteroid field.
 */
class AsteroidField extends ProductiveCelestialBody implements Serializable
{	
	private static final long	serialVersionUID	= 1L;

	public AsteroidField(ISEPServerDataBase db, String name, Location location, GameConfig gameConfig)
	{
		super(db, name, location, gameConfig, org.axan.sep.common.AsteroidField.class);
	}

	/**
	 * Full constructor.
	 */
	public AsteroidField(ISEPServerDataBase db, String name, Location location, int carbonStock, int slots, String ownerName)
	{
		super(db, name, location, carbonStock, slots, ownerName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.axan.sep.common.CelestialBody#canBuild(server.model.Building)
	 */
	@Override
	public boolean canBuild(ABuilding building)
	{
		// TODO
		return false;
	}

	/* (non-Javadoc)
	 * @see org.axan.sep.server.model.ICelestialBody#getPlayerView(int, java.lang.String, boolean)
	 */
	@Override
	public org.axan.sep.common.AsteroidField getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		return new org.axan.sep.common.AsteroidField(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getStartingCarbonStock(), getCarbonStockView(date, playerLogin, isVisible), getCarbonView(date, playerLogin, isVisible), getSlots(), getBuildingsView(date, playerLogin, isVisible), getOwnerNameView(date, playerLogin, isVisible));
	}	

}
