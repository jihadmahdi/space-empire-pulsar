package org.axan.sep.common;

import java.io.Serializable;

public class GovernmentStarship implements ISpecialUnit, Serializable
{
	public static final int CARBON_PRICE = 200;
	public static final int POPULATION_PRICE = 200;
	
	private final String name;
	
	public GovernmentStarship(String starshipName)
	{
		this.name = starshipName;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!GovernmentStarship.class.isInstance(obj)) return false;
		GovernmentStarship o = GovernmentStarship.class.cast(obj);
		
		return (this.name.equals(o.name));
	}
	
	@Override
	public int hashCode()
	{
		return (getClass().getName()+this.name).hashCode();
	}
	
	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public boolean canJoinFleet()
	{
		return true;
	}

	@Override
	public boolean isVisibleToClients()
	{
		return true;
	}
}
