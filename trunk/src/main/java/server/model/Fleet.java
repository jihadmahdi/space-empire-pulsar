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

	// Variables
	private final HashMap<Class<? extends IStarship>, Integer>		starships;
	private final Stack<common.Fleet.Move> move;

	// Views
	PlayerDatedView<HashMap<Class<? extends IStarship>, Integer>>	playersStarshipsView	= new PlayerDatedView<HashMap<Class<? extends IStarship>, Integer>>();

	public Fleet(String name, Player owner, Map<Class<? extends IStarship>, Integer> starships)
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
	}

	@Override
	public common.Unit getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			// Updates
			playersStarshipsView.updateView(playerLogin, starships, date);
		}

		return new common.Fleet(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwner(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentEstimatedLocationView(playerLogin), playersStarshipsView.getLastValue(playerLogin, null), (getOwner()!=null&&getOwner().isNamed(playerLogin)?move:null));
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
}
