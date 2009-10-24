/**
 * @author Escallier Pierre
 * @file ICelestialBody.java
 * @date 3 juin 2009
 */
package org.axan.sep.server.model;

import java.io.Serializable;

import org.axan.sep.common.Player;
import org.axan.sep.common.SEPUtils.Location;


/**
 * 
 */
interface ICelestialBody
{
	static class Key implements Serializable
	{	
		private static final long	serialVersionUID	= 1L;
		
		private final String name;
		private final Location location;
		
		public Key(String name)
		{
			this(name, null);
		}
		
		public Key(String name, Location location)
		{
			this.name = name;
			this.location = location;
		}
		
		public String getName()
		{
			return name;
		}
		
		public Location getLocation()
		{
			return location;
		}
		
		@Override
		public String toString()
		{
			return String.format("%s %s", name, (location != null ? location.toString() : "[nowhere]"));
		}
		
		@Override
		public int hashCode()
		{
			return name.hashCode();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (!Key.class.isInstance(obj)) return false;
			
			Key k = Key.class.cast(obj);
			
			boolean eq = name.equals(k.name);
			if (!eq && location != null && location.equals(k.location))
			{
				throw new Error("Two different celestial bodies cannot be located in the same area.");
			}
			
			return eq;
		}
	}
	
	/** Get this celestial body owner (null if neutral). */
	String getOwnerName();

	/**
	 * @param date
	 * @param playerLogin
	 * @param isVisible
	 * @return
	 */
	org.axan.sep.common.ICelestialBody getPlayerView(int date, String playerLogin, boolean isVisible);

	String getName();
	
	Location getLocation();
	
	Key getKey();
}
