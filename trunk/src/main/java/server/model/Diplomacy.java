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
	// DB Context
	private final DataBase db;
	
	// Constants
	private final String ownerName;
	
	// Variables
	private final Hashtable<String, common.Diplomacy.PlayerPolicies> policies = new Hashtable<String, common.Diplomacy.PlayerPolicies>();
	
	// Views
	private final PlayerDatedView<Hashtable<String, common.Diplomacy.PlayerPolicies>> playersPoliciesView = new PlayerDatedView<Hashtable<String, common.Diplomacy.PlayerPolicies>>();
	
	/**
	 * Default constructor (default policies). 
	 * @param owner
	 * @param players
	 */
	public Diplomacy(DataBase db, String ownerName)
	{
		this.db = db;
		this.ownerName = ownerName;
	}

	public PlayerPolicies getPolicies(String targetName)
	{
		if (ownerName.equals(targetName)) throw new SEPServer.SEPImplementationException("Cannot have a diplomacy toward ourselves.");
		if (!db.playerExists(targetName)) throw new SEPServer.SEPImplementationException("Player '"+targetName+"' does not exist.");
		
		if (!policies.containsKey(targetName))
		{
			policies.put(targetName, new PlayerPolicies(targetName, false, false));
		}
		
		return policies.get(targetName);
	}
	
	private void ensureDefaultPlayerPolicies()
	{
		for(String p : db.getPlayersKeySet())
		{
			if (ownerName.equals(p)) continue;
			getPolicies(p);
		}
	}
	
	public common.Diplomacy getPlayerView(int date, String playerName, boolean isVisible)
	{
		if (isVisible || ownerName.equals(playerName))
		{
			// Updates
			ensureDefaultPlayerPolicies();
			playersPoliciesView.updateView(playerName, policies, date);
		}				
		
		return new common.Diplomacy(ownerName, playersPoliciesView.getLastValue(playerName, null));
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
