/**
 * @author Escallier Pierre
 * @file Fleet.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

/**
 * Represent a fleet.
 */
public class Fleet extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static class Move
	{
		private final String destinaionCelestialBodyName;
		private final int departureDelay;
		private final boolean isAnAttack;
		
		public Move(String destinationCelestialBodyName, int departureDelay, boolean isAnAttack)
		{
			this.destinaionCelestialBodyName = destinationCelestialBodyName;
			this.departureDelay = departureDelay;
			this.isAnAttack = isAnAttack;
		}
	}
	
	private final Map<Class<? extends IStarship>, Integer> starships;
	private final Stack<Move> move;	
	private final boolean isUnasignedFleet;
	
	/**
	 * Full constructor. 
	 */
	public Fleet(boolean isVisible, int lastObservation, String name, Player owner, int[] sourceLocation, int[] destinationLocation, int[] currentEstimatedLocation, Map<Class<? extends IStarship>, Integer> starships, Stack<Move> move, boolean isUnasignedFleet)
	{
		super(isVisible, lastObservation, name, owner, sourceLocation, destinationLocation, currentEstimatedLocation);
		this.starships = starships;
		this.move = move;
		this.isUnasignedFleet = isUnasignedFleet;
	}
	
	public boolean isUnasignedFleet()
	{
		return isUnasignedFleet;
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
		sb.append("\nFleet composition :\n");
		for(Map.Entry<Class<? extends IStarship>, Integer> e : starships.entrySet())
		{
			if (e.getValue() != null && e.getValue() > 0)
			{
				sb.append("  "+e.getKey().getSimpleName()+"\t"+e.getValue()+"\n");
			}
		}
		
		return sb.toString();
	}

	public boolean isEmpty()
	{
		for(Integer i : starships.values())
		{
			if (i != null && i > 0) return false;
		}
		
		return true;
	}
}
