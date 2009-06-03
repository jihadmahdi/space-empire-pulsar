/**
 * @author Escallier Pierre
 * @file Area.java
 * @date 3 juin 2009
 */
package common;

import java.util.Set;

/**
 * This class represent an area located in the universe from a specific player point of view.
 */
public class Area implements IObservable
{
	/** Is the current area visible for the player. */
	private final boolean isVisible;	
	
	/** Last turn date this area has been visible. */
	private final int lastObservation; 
	
	/** Celestial body that fill this area (none if null). */
	private final ICelestialBody celestialBody;
	
	/**
	 * Set of units seen in this area.
	 * Each unit has its own observation turn date.
	 * But units remain until the area is visible and the unit has leaved.
	 */
	private final Set<Unit> units;
	
	/**
	 * Set of markers applied to this area.
	 */
	private final Set<IMarker> markers;

	
	/**
	 * Full constructor. 
	 */
	public Area(boolean isVisible, int lastObservation, ICelestialBody celestialBody, Set<Unit> units, Set<IMarker> markers)
	{
		this.isVisible = isVisible;
		this.lastObservation = lastObservation;
		this.celestialBody = celestialBody;
		this.units = units;
		this.markers = markers;
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
