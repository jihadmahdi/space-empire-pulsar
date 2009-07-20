/**
 * @author Escallier Pierre
 * @file Fleet.java
 * @date 3 juin 2009
 */
package common;

import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

import common.SEPUtils.RealLocation;

/**
 * Represent a fleet.
 */
public class Fleet extends Unit implements Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	public static class Move implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		private final String destinaionCelestialBodyName;
		private final RealLocation destinationLocation;
		private final int departureDelay;
		private final boolean isAnAttack;
		
		public Move(String destinationCelestialBodyName, int departureDelay, boolean isAnAttack)
		{
			this.destinaionCelestialBodyName = destinationCelestialBodyName;
			this.destinationLocation = null;
			this.departureDelay = departureDelay;
			this.isAnAttack = isAnAttack;
		}
		
		public Move(Move unLocatedMove, RealLocation destinationLocation)
		{
			this.destinaionCelestialBodyName = unLocatedMove.destinaionCelestialBodyName;
			this.destinationLocation = destinationLocation;
			this.departureDelay = unLocatedMove.departureDelay;
			this.isAnAttack = unLocatedMove.isAnAttack;				
		}
		
		public Move(Move previousMove, int decreasedDelay)
		{
			this.destinaionCelestialBodyName = previousMove.destinaionCelestialBodyName;
			this.destinationLocation = previousMove.destinationLocation;
			this.departureDelay = decreasedDelay;
			this.isAnAttack = this.isAnAttack;
		}
		
		public String getDestinationName()
		{
			return destinaionCelestialBodyName;
		}
		
		public RealLocation getDestinationLocation()
		{
			return destinationLocation;
		}
		
		public int getDelay()
		{
			return departureDelay;
		}
		
		public boolean isAnAttack()
		{
			return isAnAttack;
		}
		
		@Override
		public String toString()
		{
			return (isAnAttack?"Attack ":"Go to ")+destinaionCelestialBodyName+(departureDelay>0?" ("+departureDelay+")":"");
		}

		public Move getDecreaseDelayMove()
		{
			return new Move(this, Math.max(0, this.departureDelay - 1));
		}
	}
	
	private final Map<Class<? extends IStarship>, Integer> starships;
	private final Stack<Move> checkpoints;
	private final Move currentMove;
	private final boolean isUnasignedFleet;
	
	/**
	 * Full constructor. 
	 */
	public Fleet(boolean isVisible, int lastObservation, String name, Player owner, RealLocation sourceLocation, RealLocation destinationLocation, RealLocation currentLocation, double travellingProgress, Map<Class<? extends IStarship>, Integer> starships, Move currentMove, Stack<Move> checkpoints, boolean isUnasignedFleet)
	{
		super(isVisible, lastObservation, name, owner, sourceLocation, destinationLocation, currentLocation, travellingProgress);
		this.starships = starships;
		this.currentMove = currentMove;
		this.checkpoints = checkpoints;
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
	
	public Move getCurrentMove()
	{
		return currentMove;
	}
	
	public Stack<Move> getCheckpoints()
	{
		return checkpoints;
	}
}
