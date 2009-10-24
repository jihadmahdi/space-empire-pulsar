package org.axan.sep.server.model;

import java.io.Serializable;

/**
 * Represent a marker from a specific player point of view.
 * A marker is something used to notice the player about a specific event, but player is free to ignore and delete it.
 * i.g.: A fleet in travel has seen an enemy probe, when it arrive a marker is added in the player view to notice him about the probe location.
 * A marker has a validity flag, this flag is true unless a condition is met that should invalid the marker (
 * i.g.: A unit seen marker is valid until the unit is detected again somewhere else.
 * 
 * Note: To mark several area with the same marker just use the same instance.
 */
public interface IMarker
{
	static class Key implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		private final String name;
		private final Class<? extends IMarker> type;
		private final String observerName;
		
		public Key(String name, Class<? extends IMarker> type, String observerName)
		{
			this.name = name;
			this.type = type;
			this.observerName = observerName;
		}
		
		@Override
		public String toString()
		{
			return String.format("%s @ %s (%s)", observerName, name, type.getSimpleName());
		}
		
		@Override
		public int hashCode()
		{
			return toString().hashCode();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (!Key.class.isInstance(obj)) return false;
			
			Key k = Key.class.cast(obj);
			return type.equals(k.type) && name.equals(k.name) && observerName.equals(k.observerName);
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getObserverName()
		{
			return observerName;
		}
	}
	
	int getCreationDate();
	Key getKey();
	String getName();
	String getObserverName();
	boolean isValid();
}
