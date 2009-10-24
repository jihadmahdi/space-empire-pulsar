/**
 * @author Escallier Pierre
 * @file Area.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
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
	private transient Set<Unit> units;
	
	/**
	 * Set of markers applied to this area.
	 */
	private transient Set<IMarker> markers;

	
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
	
	public Unit getMarkedUnit(String ownerName, String unitName)
	{
		for(UnitMarker m : getMarkers(UnitMarker.class))
		{
			Unit u = m.getUnit();

			// Owner filter
			if (!ownerName.equals(u.getOwnerName())) continue;
			
			// Unit name filter
			if (u != null && unitName.equals(u.getName())) return u;
		}
		
		return null;
	}
	
	public <U extends Unit> Set<U> getUnits(Class<U> unitType)
	{
		Set<U> filteredUnits = new HashSet<U>();
		if (units != null) for(Unit u : units)
		{
			if (unitType.isInstance(u)) filteredUnits.add(unitType.cast(u));
		}
		
		filteredUnits.addAll(getMarkedUnits(unitType));
		
		return filteredUnits;
	}
	
	public <U extends Unit> Set<U> getMarkedUnits(Class<U> unitType)
	{
		Set<U> filteredMarkedUnits = new HashSet<U>();
		
		Set<UnitMarker> unitMarkers = getMarkers(UnitMarker.class);
		if (unitMarkers != null) for(UnitMarker um : unitMarkers)
		{
			if (unitType.isInstance(um.getUnit()))
			{
				filteredMarkedUnits.add(unitType.cast(um.getUnit()));
			}
		}
		
		return filteredMarkedUnits;
	}
	
	public Unit getUnit(String ownerName, String unitName)
	{
		if(units != null) for(Unit u : units)
		{
			// Owner filter
			if (!ownerName.equals(u.getOwnerName())) continue;

			// Name filter
			if (unitName.equals(u.getName())) return u;
		}
		
		return getMarkedUnit(ownerName, unitName);
	}
	
	public <M extends IMarker> Set<M> getMarkers(Class<M> markerType)
	{
		Set<M> filteredMarkers = new HashSet<M>();
		if (markers != null) for(IMarker m : markers)
		{
			if (markerType.isInstance(m))
			{
				filteredMarkers.add(markerType.cast(m));
			}
		}
		return filteredMarkers;
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
				sb.append("   ["+u.getOwnerName()+"] "+u.getName()+"\n");
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
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		
		out.writeInt(units == null?0:units.size());
		if (units != null) for(Unit u : units)
		{
			out.writeObject(u);
		}
		
		out.writeInt(markers == null?0:markers.size());
		if (markers != null) for(IMarker m : markers)
		{
			out.writeObject(m);
		}		
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		
		int nbUnits = in.readInt();
		if (nbUnits > 0) this.units = new HashSet<Unit>();
		for(int i=0; i < nbUnits; ++i)
		{
			Unit u = Unit.class.cast(in.readObject());
			units.add(u);
		}
		
		int nbMarkers = in.readInt();
		if (nbMarkers > 0) this.markers = new HashSet<IMarker>();
		for(int i=0; i < nbMarkers; ++i)
		{
			IMarker m = IMarker.class.cast(in.readObject());
			markers.add(m);
		}
	}

	private void readObjectNoData() throws ObjectStreamException
	{

	}

}
