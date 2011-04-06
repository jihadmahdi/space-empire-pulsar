package org.axan.sep.server.model;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.axan.sep.common.Diplomacy.PlayerPolicies;
import org.axan.sep.common.Diplomacy.PlayerPolicies.eForeignPolicy;
import org.axan.sep.server.SEPServer;



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
	private final Hashtable<String, org.axan.sep.common.Diplomacy.PlayerPolicies> playersPolicies = new Hashtable<String,PlayerPolicies>();
	
	// Views
	private final PlayerDatedView<Integer> playersLastObservation = new PlayerDatedView<Integer>();
	private final PlayerDatedView<Hashtable<String, org.axan.sep.common.Diplomacy.PlayerPolicies>> playersPoliciesView = new PlayerDatedView<Hashtable<String, org.axan.sep.common.Diplomacy.PlayerPolicies>>();
	
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
		
		if (!playersPolicies.containsKey(targetName))
		{
			playersPolicies.put(targetName, new PlayerPolicies(targetName, false, eForeignPolicy.NEUTRAL));
		}
				
		return playersPolicies.get(targetName);
	}
	
	private void ensureDefaultPlayerPolicies()
	{
		for(String p : db.getPlayersKeySet())
		{
			if (ownerName.equals(p)) continue;
			getPolicies(p);
		}
	}
	
	public org.axan.sep.common.Diplomacy getPlayerView(int date, String playerName, boolean isVisible)
	{
		if (isVisible || ownerName.equals(playerName))
		{
			// Updates
			ensureDefaultPlayerPolicies();
			playersPoliciesView.updateView(playerName, playersPolicies, date);
			playersLastObservation.updateView(playerName, date, date);
		}				
		
		int lastObservation = playersLastObservation.getLastValue(playerName, -1);				
		
		return new org.axan.sep.common.Diplomacy(isVisible, lastObservation, ownerName, playersPoliciesView.getLastValue(playerName, null));
	}

	public void update(Map<String,PlayerPolicies> newPolicies)
	{
		playersPolicies.clear();		
		
		for(String playerLogin : newPolicies.keySet())
		{
			playersPolicies.put(playerLogin, newPolicies.get(playerLogin));
		}
	}
}
