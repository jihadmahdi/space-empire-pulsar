/**
 * @author Escallier Pierre
 * @file Nebula.java
 * @date 1 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;

import org.axan.sep.common.GameConfig;
import org.axan.sep.common.SEPUtils.Location;


/**
 * Represent a Nebula.
 */
class Nebula extends ProductiveCelestialBody implements Serializable
{	
	private static final long	serialVersionUID	= 1L;

	public Nebula(DataBase db, String name, Location location, GameConfig gameConfig)
	{
		super(db, name, location, gameConfig, org.axan.sep.common.Nebula.class);
	}
	
	/**
	 * Full constructor.
	 */
	public Nebula(DataBase db, String name, Location location, int carbonStock, int slots, String ownerName)
	{
		super(db, name, location, carbonStock, slots, ownerName);
	}

	/* (non-Javadoc)
	 * @see org.axan.sep.common.CelestialBody#canBuild(server.model.Building)
	 */
	@Override
	public boolean canBuild(ABuilding building)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.axan.sep.server.model.ICelestialBody#getPlayerView(int, java.lang.String, boolean)
	 */
	@Override
	public org.axan.sep.common.Nebula getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		return new org.axan.sep.common.Nebula(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getStartingCarbonStock(), getCarbonStockView(date, playerLogin, isVisible), getCarbonView(date, playerLogin, isVisible), getSlots(), getBuildingsView(date, playerLogin, isVisible), getOwnerNameView(date, playerLogin, isVisible));
	}

}
