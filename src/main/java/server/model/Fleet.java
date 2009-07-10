package server.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import common.GovernmentStarship;
import common.IStarship;
import common.Player;
import common.SEPUtils;
import common.SEPUtils.Location;

public class Fleet extends Unit implements Serializable
{
	private static final long										serialVersionUID		= 1L;

	private final boolean isUnassigned;
	
	// Variables
	private final HashMap<Class<? extends IStarship>, Integer>		starships;
	private final Stack<common.Fleet.Move> checkpoints;
	private common.Fleet.Move currentMove;

	// Views
	PlayerDatedView<HashMap<Class<? extends IStarship>, Integer>>	playersStarshipsView	= new PlayerDatedView<HashMap<Class<? extends IStarship>, Integer>>();

	public Fleet(String name, Player owner, Map<Class<? extends IStarship>, Integer> starships, boolean isUnassigned)
	{
		super(name, owner);
		
		if (starships != null)
		{
			this.starships = new HashMap<Class<? extends IStarship>, Integer>(starships);
		}
		else
		{
			this.starships = new HashMap<Class<? extends IStarship>, Integer>();
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
		}				
		
		return new common.Fleet(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwner(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentEstimatedLocationView(playerLogin), playersStarshipsView.getLastValue(playerLogin, null), (getOwner()!=null&&getOwner().isNamed(playerLogin)?currentMove:null) ,(getOwner()!=null&&getOwner().isNamed(playerLogin)?checkpoints:null), isUnassigned);
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
	
	public Map<Class<? extends IStarship>, Integer> getComposition()
	{
		return Collections.unmodifiableMap(starships);
	}
	
	public boolean isEmpty()
	{
		for(Map.Entry<Class<? extends IStarship>, Integer> e : starships.entrySet())
		{
			if (e.getValue() != null && e.getValue() > 0) return false;
		}
		
		return true;
	}

	public void merge(Map<Class<? extends IStarship>, Integer> starshipsToMake)
	{
		for(Map.Entry<Class<? extends IStarship>, Integer> e : starshipsToMake.entrySet())
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
	}

	public void remove(Map<Class<? extends IStarship>, Integer> fleetToForm)
	{
		for(Map.Entry<Class<? extends IStarship>, Integer> e : fleetToForm.entrySet())
		{
			if (e.getValue() == null || e.getValue() <= 0) continue;
			
			if (!starships.containsKey(e.getKey())) throw new Error("Fleet remove error (does not contain starship type '"+e.getKey().getSimpleName()+"'");
			if (starships.get(e.getKey()) < e.getValue()) throw new Error("Fleet remove error (does not contain enough starships of type '"+e.getKey().getSimpleName()+"'");
			
			starships.put(e.getKey(), starships.get(e.getKey()) - e.getValue());				
		}
	}
	
	public void updateMoveOrder(Stack<common.Fleet.Move> newCheckpoints)
	{
		checkpoints.removeAllElements();
		
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
	public boolean startMove(Location currentLocation, GameBoard currentGameBoard)
	{
		if ((currentMove == null || currentMove.getDestinationLocation().equals(currentLocation)) && checkpoints.size() > 0)
		{
			currentMove = checkpoints.firstElement();
			checkpoints.removeElementAt(0);
			
			setSourceLocation(currentLocation);
			setDestinationLocation(currentGameBoard.getCelestialBodyLocation(currentMove.getDestinationName()));
			setCurrentLocation(currentLocation);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void endMove(Location currentLocation, GameBoard gameBoard)
	{
		setDestinationLocation(null);
		currentMove = null; // Needed for the next startMove call.
	}
	
	@Override
	public double getSpeed()
	{
		// TODO, compute total speed from fleet composition
		return 3;
	}	
}
