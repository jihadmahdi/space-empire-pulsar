package org.axan.sep.server.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.axan.sep.common.GovernmentStarship;
import org.axan.sep.common.Player;
import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.SEPServer;
import org.axan.sep.server.model.Unit.Key;



public class Fleet extends Unit implements Serializable
{
	public static class Key extends Unit.Key implements Serializable
	{		
		private static final long	serialVersionUID	= 1L;

		public Key(String name, String ownerName)
		{
			super(name, ownerName);
		}
	}
	
	private static final long										serialVersionUID		= 1L;

	private final boolean isUnassigned;
	
	// Variables
	private final HashMap<org.axan.sep.common.StarshipTemplate, Integer> starships;
	private final HashSet<org.axan.sep.common.ISpecialUnit> specialUnits;
	private final Stack<org.axan.sep.common.Fleet.Move> checkpoints;
	private org.axan.sep.common.Fleet.Move currentMove;

	// Views
	PlayerDatedView<HashMap<org.axan.sep.common.StarshipTemplate, Integer>>	playersStarshipsView	= new PlayerDatedView<HashMap<org.axan.sep.common.StarshipTemplate, Integer>>();
	PlayerDatedView<HashSet<org.axan.sep.common.ISpecialUnit>> playersSpecialUnitsView = new PlayerDatedView<HashSet<org.axan.sep.common.ISpecialUnit>>();

	public Fleet(DataBase db, String name, String ownerName, RealLocation sourceLocation, Map<org.axan.sep.common.StarshipTemplate, Integer> starships, Set<org.axan.sep.common.ISpecialUnit> specialUnits, boolean isUnassigned)
	{
		this(db, new Key(name, ownerName), sourceLocation, starships, specialUnits, isUnassigned);		
	}
	
	public Fleet(DataBase db, Key key, RealLocation sourceLocation, Map<org.axan.sep.common.StarshipTemplate, Integer> starships, Set<org.axan.sep.common.ISpecialUnit> specialUnits, boolean isUnassigned)
	{
		super(db, key, sourceLocation);
		
		if (starships != null)
		{
			this.starships = new HashMap<org.axan.sep.common.StarshipTemplate, Integer>(starships);
		}
		else
		{
			this.starships = new HashMap<org.axan.sep.common.StarshipTemplate, Integer>();
		}
		
		if (specialUnits != null)
		{
			this.specialUnits = new HashSet<org.axan.sep.common.ISpecialUnit>(specialUnits);
		}
		else
		{
			this.specialUnits = new HashSet<org.axan.sep.common.ISpecialUnit>();
		}
		
		this.checkpoints = new Stack<org.axan.sep.common.Fleet.Move>();
		this.isUnassigned = isUnassigned;
	}	

	@Override
	public org.axan.sep.common.Fleet getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			// Updates
			playersStarshipsView.updateView(playerLogin, starships, date);
			playersSpecialUnitsView.updateView(playerLogin, specialUnits, date);
			
		}				
		
		return new org.axan.sep.common.Fleet(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwnerName(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin), playersStarshipsView.getLastValue(playerLogin, null), playersSpecialUnitsView.getLastValue(playerLogin, null), (playerLogin.equals(getOwnerName()) ? currentMove : null) ,(playerLogin.equals(getOwnerName())?checkpoints:null), isUnassigned);
	}

	public boolean isGovernmentFleet()
	{
		for(org.axan.sep.common.ISpecialUnit specialUnit : specialUnits)
		{
			if (org.axan.sep.common.GovernmentStarship.class.isInstance(specialUnit))
			{
				return true;
			}
		}

		return false;
	}
	
	public void removeGovernment()
	{
		for(org.axan.sep.common.ISpecialUnit specialUnit : specialUnits)
		{
			if (org.axan.sep.common.GovernmentStarship.class.isInstance(specialUnit))
			{
				specialUnits.remove(specialUnit);
				return;
			}
		}
	}
	
	public boolean isUnassignedFleet()
	{
		return isUnassigned;
	}
	
	public Map<org.axan.sep.common.StarshipTemplate, Integer> getStarships()
	{
		return Collections.unmodifiableMap(starships);
	}
	
	public Set<org.axan.sep.common.ISpecialUnit> getSpecialUnits()
	{
		return Collections.unmodifiableSet(specialUnits);
	}
	
	public boolean isEmpty()
	{
		for(Map.Entry<org.axan.sep.common.StarshipTemplate, Integer> e : starships.entrySet())
		{
			if (e.getValue() != null && e.getValue() > 0) return false;
		}
		
		return true;
	}

	public void merge(Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMerge, Set<org.axan.sep.common.ISpecialUnit> specialUnitsToMerge)
	{
		if (starshipsToMerge != null) for(Map.Entry<org.axan.sep.common.StarshipTemplate, Integer> e : starshipsToMerge.entrySet())
		{
			if (e.getValue() == null || e.getValue() <= 0) continue;
			
			if (starships.containsKey(e.getKey()))
			{
				starships.put(e.getKey(), starships.get(e.getKey())+e.getValue());
			}
			else
			{
				starships.put(e.getKey(), e.getValue());
			}
		}
		
		// TODO: Check if it's ok.
		if (specialUnitsToMerge != null) for(org.axan.sep.common.ISpecialUnit u : specialUnitsToMerge)
		{
			if (u == null) continue;
			
			if (!specialUnits.contains(u))
			{
				specialUnits.add(u);
			}
		}
	}

	public void remove(Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToForm, Set<org.axan.sep.common.ISpecialUnit> specialUnitsToForm)
	{
		if (fleetToForm != null) for(Map.Entry<org.axan.sep.common.StarshipTemplate, Integer> e : fleetToForm.entrySet())
		{
			if (e.getValue() == null || e.getValue() <= 0) continue;
			
			if (!starships.containsKey(e.getKey())) throw new Error("Fleet remove error (does not contain starship type '"+e.getKey().getName()+"'");
			if (starships.get(e.getKey()) < e.getValue()) throw new Error("Fleet remove error (does not contain enough starships of type '"+e.getKey().getName()+"'");
			
			starships.put(e.getKey(), starships.get(e.getKey()) - e.getValue());				
		}
		
		// TODO: Check if it's ok.
		if (specialUnitsToForm != null) for(org.axan.sep.common.ISpecialUnit u : specialUnitsToForm)
		{
			if (u == null) continue;
			
			specialUnits.remove(u);
		}
	}
	
	public void updateMoveOrder(Stack<org.axan.sep.common.Fleet.Move> newCheckpoints)
	{
		checkpoints.removeAllElements();
		
		if (!isMoving()) currentMove = null;
		
		for(org.axan.sep.common.Fleet.Move checkpoint : newCheckpoints)
		{
			if (checkpoints.size() > 0 && checkpoints.peek().getDestinationName().equals(checkpoint.getDestinationName()))
			{
				checkpoints.pop();
			}
			
			checkpoints.push(checkpoint);
		}
	}
	
	@Override
	public boolean startMove()
	{		
		if ((currentMove == null || currentMove.getDestinationLocation().equals(getRealLocation())) && checkpoints.size() > 0)
		{
			if (super.startMove())
			{
				currentMove = checkpoints.firstElement();
				checkpoints.removeElementAt(0);												
			}
		}
		
		if (currentMove != null)
		{
			if (currentMove.getDelay() == 0)
			{
				setDestinationLocation(db.getCelestialBody(currentMove.getDestinationName()).getLocation().asRealLocation());
				return true;
			}
			else
			{
				currentMove = currentMove.getDecreaseDelayMove();
			}
		}
		
		return false;
	}
	
	@Override
	public void endMove()
	{	
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(getRealLocation().asLocation(), ProductiveCelestialBody.class);
		if (productiveCelestialBody == null) throw new SEPServer.SEPImplementationException("Cannot set conflict on location '"+getRealLocation()+"', no ProductiveCelestialBody found there.");
		
		if (currentMove.isAnAttack())
		{				
			productiveCelestialBody.addConflictInititor(getOwnerName());
		}		
		
		currentMove = null; // Needed for the next startMove call.
		super.endMove();
	}
	
	@Override
	public double getSpeed()
	{
		// TODO, compute total speed from fleet composition
		return 3;
	}	
}