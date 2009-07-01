package server.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import common.GovernmentStarship;
import common.IStarship;
import common.Player;

public class Fleet extends Unit implements Serializable
{
	private static final long										serialVersionUID		= 1L;

	private final boolean isUnassigned;
	
	// Variables
	private final HashMap<Class<? extends IStarship>, Integer>		starships;
	private final Stack<common.Fleet.Move> move;

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
		
		this.move = new Stack<common.Fleet.Move>();
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

		return new common.Fleet(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwner(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentEstimatedLocationView(playerLogin), playersStarshipsView.getLastValue(playerLogin, null), (getOwner()!=null&&getOwner().isNamed(playerLogin)?move:null), isUnassigned);
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
}
