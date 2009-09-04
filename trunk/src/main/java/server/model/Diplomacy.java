package server.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import server.SEPServer;

import common.Player;
import common.Diplomacy.PlayerPolicies;

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
	public Diplomacy(Player owner, Set<String> playersNames)
	{
		this.owner = owner;
		
		for(String player : playersNames)
		{
			if (player.compareTo(owner.getName()) == 0)
			{
				policies.put(player, new common.Diplomacy.PlayerPolicies(player, false, false));
			}
		}
	}

	public PlayerPolicies getPolicies(String targetLogin)
	{
		if (owner.isNamed(targetLogin)) throw new SEPServer.SEPImplementationException("Cannot have a diplomacy toward ourselves.");
		if (!policies.containsKey(targetLogin))
		{			
			policies.put(targetLogin, new PlayerPolicies(targetLogin, false, false));
		}
		
		return policies.get(targetLogin);
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
