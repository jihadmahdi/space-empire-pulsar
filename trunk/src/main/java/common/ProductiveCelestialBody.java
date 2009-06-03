/**
 * @author Escallier Pierre
 * @file FilteredCelestialBody.java
 * @date 3 juin 2009
 */
package common;

import java.util.Set;

/**
 * Represent a productive celestial body. That is to say a celestial body that can produce carbon resource and has slots free to build.
 */
public abstract class ProductiveCelestialBody implements ICelestialBody
{
	private final boolean isVisible;	
	private final int lastObservation; 
	
	// Constant
	private final String name;
	private final int carbonStock;
	private final int slots;
	
	// Only if visible
	private final int carbon;
	private final Set<IBuilding> buildings;
	private final Player owner;
	
	/**
	 * Full constructor.
	 */
	public ProductiveCelestialBody(boolean isVisible, int lastObservation, String name, int carbonStock, int carbon, int slots, Set<IBuilding> buildings, Player owner)
	{
		this.isVisible = isVisible;
		this.lastObservation = lastObservation;
		this.name = name;
		this.carbonStock = carbonStock;
		this.carbon = carbon;
		this.slots = slots;
		this.buildings = buildings;
		this.owner = owner;
	}
	
	/* (non-Javadoc)
	 * @see common.Observable#getLastObservation()
	 */
	@Override
	public int getLastObservation()
	{
		return lastObservation;
	}

	/* (non-Javadoc)
	 * @see common.Observable#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return isVisible;
	}
}
