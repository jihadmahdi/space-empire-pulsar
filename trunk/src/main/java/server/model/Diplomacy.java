package server.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import common.Player;

/**
 * This class respresent polices of a player at instant time in the game.
 */
public class Diplomacy implements Serializable
{
	// Constants
	private final Player owner;
	
	// Variables
	private final Hashtable<String, common.Diplomacy.PlayerPolicies> policies = new Hashtable<String, common.Diplomacy.PlayerPolicies>();
	
	// Views
	private final PlayerDatedView<Hashtable<String, common.Diplomacy.PlayerPolicies>> playersPoliciesView = new PlayerDatedView<Hashtable<String, common.Diplomacy.PlayerPolicies>>();
	
	/**
	 * Default constructor (default policies). 
	 * @param owner
	 * @param players
	 */
	public Diplomacy(Player owner, Set<Player> players)
	{
		this.owner = owner;
		
		for(Player player : players)
		{
			if (!player.isNamed(owner.getName()))
			{
				policies.put(player.getName(), new common.Diplomacy.PlayerPolicies(player.getName(), false, false));
			}
		}
	}

	public common.Diplomacy getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible || owner.isNamed(playerLogin))
		{
			// Updates
			playersPoliciesView.updateView(playerLogin, policies, date);
		}				
		
		return new common.Diplomacy(owner, playersPoliciesView.getLastValue(playerLogin, null));
	}

	public void update(common.Diplomacy newDiplomacy)
	{
		policies.clear();
		for(String playerLogin : newDiplomacy.targetSet())
		{
			policies.put(playerLogin, newDiplomacy.getPolicies(playerLogin));
		}
	}
}
