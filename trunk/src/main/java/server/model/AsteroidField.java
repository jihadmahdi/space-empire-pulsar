/**
 * @author Escallier Pierre
 * @file AsteroidField.java
 * @date 1 juin 2009
 */
package server.model;

import common.CelestialBody;
import common.Player;

/**
 * This class represent an asteroid field.
 */
public class AsteroidField extends CelestialBody
{
	public static final int CARBON_MIN = 60*1000;
	
	public static final int CARBON_MAX = 300*1000;
	
	public static final int SLOTS_MIN = 3;
	
	public static final int SLOTS_MAX = 6;

	public static final float GENERATION_RATE = (float) 0.50;
	
	/**
	 * Full constructor.
	 */
	public AsteroidField(int carbonStock, int slots, Player owner)
	{
		super(carbonStock, slots, owner);
	}
	
	/* (non-Javadoc)
	 * @see common.CelestialBody#canBuild(server.model.Building)
	 */
	@Override
	public boolean canBuild(Building building)
	{
		// TODO
		return false;
	}

}
