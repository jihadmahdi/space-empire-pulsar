/**
 * @author Escallier Pierre
 * @file Area.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * This class represent an area located in the universe from a specific player point of view.
 */
public class Area implements IObservable, Serializable
{
	private static final long	serialVersionUID	= 1L;

	/** Is the current area visible for the player. */
	private final boolean isVisible;	
	
	/** Last turn date this area has been visible. */
	private final int lastObservation; 
	
	/** True if this area is filled with sun. */
	private final boolean isSun;
	
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

	
	public Area()
	{
		this(false, -1, false, null, null, null);
	}
	
	/**
	 * Full constructor. 
	 */
	public Area(boolean isVisible, int lastObservation, boolean isSun, ICelestialBody celestialBody, Set<Unit> units, Set<IMarker> markers)
	{
		this.isVisible = isVisible;
		this.lastObservation = lastObservation;
		this.isSun = isSun;
		this.celestialBody = celestialBody;
		this.units = (units == null)?null:Collections.unmodifiableSet(units);
		this.markers = (markers == null)?null:Collections.unmodifiableSet(markers);
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
	
	public ICelestialBody getCelestialBody()
	{
		return celestialBody;
	}
	
	public Set<Unit> getUnits()
	{
		return units;
	}
	
	public Set<IMarker> getMarkers()
	{
		return markers;
	}
	
	public boolean isSun()
	{
		return isSun;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		if (isVisible)
		{
			sb.append("currently observed");
		}
		else
		{
			sb.append((lastObservation < 0)?"never been observed":"last observation on turn "+lastObservation);
		}
		sb.append("\n");
		
		if (isSun)
		{
			sb.append("Sun\n");
		}
		else if (celestialBody != null)
		{
			sb.append(celestialBody+"\n");
		}
		
		if (units != null && !units.isEmpty())
		{
			sb.append("Units :\n");
			for(Unit u : units)
			{
				sb.append("   ["+u.getOwner().getName()+"] "+u.getName()+"\n");
			}
		}
		
		if (markers != null && !markers.isEmpty())
		{
			sb.append("Markers :\n");
			for(IMarker m : markers)
			{
				sb.append(m);
			}
		}
		
		return sb.toString();
	}
}
