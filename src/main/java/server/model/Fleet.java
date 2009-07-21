package server.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import common.GovernmentStarship;
import common.Player;
import common.SEPUtils;
import common.SEPUtils.RealLocation;

public class Fleet extends Unit implements Serializable
{
	private static final long										serialVersionUID		= 1L;

	private final boolean isUnassigned;
	
	// Variables
	private final HashMap<common.StarshipTemplate, Integer> starships;
	private final HashSet<common.ISpecialUnit> specialUnits;
	private final Stack<common.Fleet.Move> checkpoints;
	private common.Fleet.Move currentMove;

	// Views
	PlayerDatedView<HashMap<common.StarshipTemplate, Integer>>	playersStarshipsView	= new PlayerDatedView<HashMap<common.StarshipTemplate, Integer>>();
	PlayerDatedView<HashSet<common.ISpecialUnit>> playersSpecialUnitsView = new PlayerDatedView<HashSet<common.ISpecialUnit>>();

	public Fleet(String name, Player owner, RealLocation sourceLocation, Map<common.StarshipTemplate, Integer> starships, Set<common.ISpecialUnit> specialUnits, boolean isUnassigned)
	{
		super(name, owner, sourceLocation);
		
		if (starships != null)
		{
			this.starships = new HashMap<common.StarshipTemplate, Integer>(starships);
		}
		else
		{
			this.starships = new HashMap<common.StarshipTemplate, Integer>();
		}
		
		if (specialUnits != null)
		{
			this.specialUnits = new HashSet<common.ISpecialUnit>(specialUnits);
		}
		else
		{
			this.specialUnits = new HashSet<common.ISpecialUnit>();
		}
		
		this.checkpoints = new Stack<common.Fleet.Move>();
		this.isUnassigned = isUnassigned;
	}

	@Override
	public common.Fleet getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			// Updates
			playersStarshipsView.updateView(playerLogin, starships, date);
			playersSpecialUnitsView.updateView(playerLogin, specialUnits, date);
		}				
		
		return new common.Fleet(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwner(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin), playersStarshipsView.getLastValue(playerLogin, null), playersSpecialUnitsView.getLastValue(playerLogin, null), (getOwner()!=null&&getOwner().isNamed(playerLogin)?currentMove:null) ,(getOwner()!=null&&getOwner().isNamed(playerLogin)?checkpoints:null), isUnassigned);
	}

	public boolean isGovernmentFleet()
	{
		for(common.ISpecialUnit specialUnit : specialUnits)
		{
			if (common.GovernmentStarship.class.isInstance(specialUnit))
			{
				return true;
			}
		}

		return false;
	}
	
	public Map<common.StarshipTemplate, Integer> getStarships()
	{
		return Collections.unmodifiableMap(starships);
	}
	
	public Set<common.ISpecialUnit> getSpecialUnits()
	{
		return Collections.unmodifiableSet(specialUnits);
	}
	
	public boolean isEmpty()
	{
		for(Map.Entry<common.StarshipTemplate, Integer> e : starships.entrySet())
		{
			if (e.getValue() != null && e.getValue() > 0) return false;
		}
		
		return true;
	}

	public void merge(Map<common.StarshipTemplate, Integer> starshipsToMerge, Set<common.ISpecialUnit> specialUnitsToMerge)
	{
		if (starshipsToMerge != null) for(Map.Entry<common.StarshipTemplate, Integer> e : starshipsToMerge.entrySet())
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
		if (specialUnitsToMerge != null) for(common.ISpecialUnit u : specialUnitsToMerge)
		{
			if (u == null) continue;
			
			if (!specialUnits.contains(u))
			{
				specialUnits.add(u);
			}
		}
	}

	public void remove(Map<common.StarshipTemplate, Integer> fleetToForm, Set<common.ISpecialUnit> specialUnitsToForm)
	{
		if (fleetToForm != null) for(Map.Entry<common.StarshipTemplate, Integer> e : fleetToForm.entrySet())
		{
			if (e.getValue() == null || e.getValue() <= 0) continue;
			
			if (!starships.containsKey(e.getKey())) throw new Error("Fleet remove error (does not contain starship type '"+e.getKey().getName()+"'");
			if (starships.get(e.getKey()) < e.getValue()) throw new Error("Fleet remove error (does not contain enough starships of type '"+e.getKey().getName()+"'");
			
			starships.put(e.getKey(), starships.get(e.getKey()) - e.getValue());				
		}
		
		// TODO: Check if it's ok.
		if (specialUnitsToForm != null) for(common.ISpecialUnit u : specialUnitsToForm)
		{
			if (u == null) continue;
			
			specialUnits.remove(u);
		}
	}
	
	public void updateMoveOrder(Stack<common.Fleet.Move> newCheckpoints)
	{
		checkpoints.removeAllElements();
		
		if (!isMoving()) currentMove = null;
		
		for(common.Fleet.Move checkpoint : newCheckpoints)
		{
			if (checkpoints.size() > 0 && checkpoints.peek().getDestinationName().compareTo(checkpoint.getDestinationName()) == 0)
			{
				checkpoints.pop();
			}
			
			checkpoints.push(checkpoint);
		}
	}
	
	@Override
	public boolean startMove(RealLocation currentLocation, GameBoard currentGameBoard)
	{
		if ((currentMove == null || currentMove.getDestinationLocation().equals(currentLocation)) && checkpoints.size() > 0)
		{
			if (super.startMove(currentLocation, currentGameBoard))
			{
				currentMove = checkpoints.firstElement();
				checkpoints.removeElementAt(0);												
			}
		}
		
		if (currentMove != null)
		{
			if (currentMove.getDelay() == 0)
			{
				setDestinationLocation(currentGameBoard.getCelestialBodyLocation(currentMove.getDestinationName()));
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
	public void endMove(RealLocation currentLocation, GameBoard gameBoard)
	{
		setDestinationLocation(null);
		if (currentMove.isAnAttack())
		{
			gameBoard.initiateConflict(currentLocation, getOwnerName());
		}		
		currentMove = null; // Needed for the next startMove call.
		super.endMove(currentLocation, gameBoard);
	}
	
	@Override
	public double getSpeed()
	{
		// TODO, compute total speed from fleet composition
		return 3;
	}	
}
