package server.model;

import java.util.HashMap;
import java.util.Map;

import common.GovernmentStarship;
import common.IStarship;
import common.Player;

public class Fleet extends Unit
{		
	// Variables
	private HashMap<Class<? extends IStarship>, Integer> starships = new HashMap<Class<? extends IStarship>, Integer>();

	// Views
	PlayerDatedView<HashMap<Class<? extends IStarship>, Integer>> playersStarshipsView = new PlayerDatedView<HashMap<Class<? extends IStarship>, Integer>>();
	
	public Fleet(String name, Player owner)
	{
		super(name, owner);
	}
	
	@Override
	public common.Unit getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			// Updates
			playersStarshipsView.updateView(playerLogin, starships, date);
		}
		
		return new common.Fleet(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwner(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentEstimatedLocationView(playerLogin), playersStarshipsView.getLastValue(playerLogin, null));
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
}
