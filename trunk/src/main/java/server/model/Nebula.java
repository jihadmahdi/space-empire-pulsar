/**
 * @author Escallier Pierre
 * @file Nebula.java
 * @date 1 juin 2009
 */
package server.model;

import common.CelestialBody;
import common.Player;

/**
 * Represent a Nebula.
 */
public class Nebula extends CelestialBody
{
	public static final int CARBON_MIN = 100*1000;
	
	public static final int CARBON_MAX = 500*1000;
	
	public static final int SLOTS_MIN = 2;
	
	public static final int SLOTS_MAX = 4;
	
	public static final float GENERATION_RATE = (float) 0.20;
	
	/**
	 * Full constructor.
	 */
	public Nebula(int carbonStock, int slots, Player owner)
	{
		super(carbonStock, slots, owner);
	}

	/* (non-Javadoc)
	 * @see common.CelestialBody#canBuild(server.model.Building)
	 */
	@Override
	public boolean canBuild(Building building)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
