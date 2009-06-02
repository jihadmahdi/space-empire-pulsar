/**
 * @author Escallier Pierre
 * @file CelestialBody.java
 * @date 29 mai 2009
 */
package common;

import java.util.HashSet;
import java.util.Set;

import server.model.Building;

/**
 * Abstract class that represent a celestial body.
 */
public abstract class CelestialBody
{
	protected final int carbonStock;
	protected final int slots;
	protected Player owner;
	protected final Set<Building> buildings;
	
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
	public CelestialBody(int carbonStock, int slots, Player owner)
	{
		this.carbonStock = carbonStock;
		this.slots = slots;
		this.owner = owner;
		buildings = new HashSet<Building>();
	}
	
	/**
	 * Build the given building on the current celestial body.
	 * Check for free slot to build, abstract method canBuild(Building)id called on the final instance to check if this building can be build on this celestial body.
	 * @param building
	 * @throws CelestialBodyBuildException
	 */
	public void build(Building building) throws CelestialBodyBuildException
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
	
	abstract public boolean canBuild(Building building);
}
