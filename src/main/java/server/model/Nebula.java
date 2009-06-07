/**
 * @author Escallier Pierre
 * @file Nebula.java
 * @date 1 juin 2009
 */
package server.model;

import common.GameConfig;
import common.ICelestialBody;
import common.Player;

/**
 * Represent a Nebula.
 */
class Nebula extends ProductiveCelestialBody
{	
	public Nebula(String name, GameConfig gameConfig)
	{
		super(name, gameConfig, common.Nebula.class);
	}
	
	/**
	 * Full constructor.
	 */
	public Nebula(String name, int carbonStock, int slots, Player owner)
	{
		super(name, carbonStock, slots, owner);
	}

	/* (non-Javadoc)
	 * @see common.CelestialBody#canBuild(server.model.Building)
	 */
	@Override
	public boolean canBuild(IBuilding building)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see server.model.ICelestialBody#getPlayerView(int, java.lang.String, boolean)
	 */
	@Override
	public common.Nebula getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		return new common.Nebula(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getCarbonStock(), getCarbonView(date, playerLogin, isVisible), getSlots(), getBuildingsView(date, playerLogin, isVisible), getOwnerView(date, playerLogin, isVisible));
	}

}