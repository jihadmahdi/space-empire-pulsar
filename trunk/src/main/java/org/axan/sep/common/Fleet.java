/**
 * @author Escallier Pierre
 * @file Fleet.java
 * @date 3 juin 2009
 */
package org.axan.sep.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.axan.sep.common.SEPUtils.RealLocation;


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
			this.isAnAttack = false;
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
	
	private final Map<StarshipTemplate, Integer> starships;
	private final Set<ISpecialUnit> specialUnits;
	private final Stack<Move> checkpoints;
	private Move currentMove;
	private final boolean isUnasignedFleet;
	
	/**
	 * Full constructor. 
	 */
	public Fleet(boolean isVisible, int lastObservation, String name, String ownerName, RealLocation sourceLocation, RealLocation destinationLocation, RealLocation currentLocation, double travellingProgress, double speed, Map<StarshipTemplate, Integer> starships, Set<ISpecialUnit> specialUnits, Move currentMove, Stack<Move> checkpoints, boolean isUnasignedFleet)
	{
		super(isVisible, lastObservation, name, ownerName, sourceLocation, destinationLocation, currentLocation, travellingProgress, speed);
		this.starships = (starships == null ? new HashMap<StarshipTemplate, Integer>() : starships);
		this.specialUnits = (specialUnits == null ? new HashSet<ISpecialUnit>() : specialUnits);
		this.currentMove = currentMove;
		this.checkpoints = (checkpoints == null ? new Stack<Move>() : checkpoints);
		this.isUnasignedFleet = isUnasignedFleet;
	}
	
	public boolean isUnasignedFleet()
	{
		return isUnasignedFleet;
	}
	
	public boolean isGovernmentFleet()
	{
		for(ISpecialUnit specialUnit : specialUnits)
		{
			if (GovernmentStarship.class.isInstance(specialUnit))
			{
				return true;
			}
		}
		
		return false;
	}
	
	void removeGovernment()
	{
		for(ISpecialUnit specialUnit : specialUnits)
		{
			if (GovernmentStarship.class.isInstance(specialUnit))
			{
				specialUnits.remove(specialUnit);
				return;
			}
		}
		
		throw new SEPCommonImplementationException("Government starship not found.");
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append("\nFleet composition :\n");
		for(Map.Entry<StarshipTemplate, Integer> e : starships.entrySet())
		{
			if (e.getValue() != null && e.getValue() > 0)
			{
				sb.append("  "+e.getKey().getName()+"\t"+e.getValue()+"\n");
			}
		}
		
		if (specialUnits != null)
		{
			sb.append("\nSpecial units :\n");
			
			for(ISpecialUnit u : specialUnits)
			{
				sb.append("  "+u.toString()+"\n");
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

	public int getTotalQt()
	{
		int qt = 0;
		for(Integer i : starships.values())
		{
			qt += Math.max(0, i);
		}
		
		return qt;
	}
	
	public Map<StarshipTemplate, Integer> getStarships()
	{
		return Collections.unmodifiableMap(starships);
	}
	
	public Set<ISpecialUnit> getSpecialUnits()
	{
		return Collections.unmodifiableSet(specialUnits);
	}
	
	public void updateMoveOrder(Stack<Move> newCheckpoints)
	{
		checkpoints.removeAllElements();
		
		if (!isMoving()) currentMove = null;
		
		for(Move checkpoint : newCheckpoints)
		{
			if (checkpoints.size() > 0 && checkpoints.peek().getDestinationName().equals(checkpoint.getDestinationName()))
			{
				checkpoints.pop();
			}
			
			checkpoints.push(checkpoint);
		}
	}
}
