/**
 * @author Escallier Pierre
 * @file Fleet.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.Map;

/**
 * Represent a fleet.
 */
public class Fleet extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private final Map<Class<? extends IStarship>, Integer> starships;
	
	/**
	 * Full constructor. 
	 */
	public Fleet(boolean isVisible, int lastObservation, String name, Player owner, int[] sourceLocation, int[] destinationLocation, int[] currentEstimatedLocation, Map<Class<? extends IStarship>, Integer> starships)
	{
		super(isVisible, lastObservation, name, owner, sourceLocation, destinationLocation, currentEstimatedLocation);
		this.starships = starships;
	}
	
	public boolean isGovernmentFleet()
	{
		for(Class<? extends IStarship> starship : starships.keySet())
		{
			if (GovernmentStarship.class.equals(starship))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append("\nFleet composition :+\n");
		for(Map.Entry<Class<? extends IStarship>, Integer> e : starships.entrySet())
		{
			if (e.getValue() != null && e.getValue() > 0)
			{
				sb.append("\t"+e.getKey().getSimpleName()+"\t"+e.getValue()+"\n");
			}
		}
		
		return sb.toString();
	}
}
