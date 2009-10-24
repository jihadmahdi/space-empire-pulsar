package org.axan.sep.common;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Observer;
import java.util.Set;


import sun.nio.ch.EPollSelectorProvider;

public class Diplomacy implements Serializable, IObservable
{
	private static final long	serialVersionUID	= 1L;

	/** Is the current area visible for the player. */
	private final boolean isVisible;	
	
	/** Last turn date this area has been visible. */
	private final int lastObservation; 
	
	private final String ownerName;
	private final Map<String, PlayerPolicies> playersPolicies;	
	
	public Diplomacy(boolean isVisible, int lastObservation, String ownerName, Map<String,PlayerPolicies> playersPolicies)
	{
		this.isVisible = isVisible;
		this.lastObservation = lastObservation;
		this.ownerName = ownerName;
		this.playersPolicies = playersPolicies;
	}
	
	public PlayerPolicies getPolicies(String targetName)
	{
		return playersPolicies == null ? null : playersPolicies.get(targetName);
	}
	
	public static class PlayerPolicies implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		public static enum eForeignPolicy
		{
			NEUTRAL, HOSTILE, HOSTILE_IF_OWNER;
		};
		
		private final String targetLogin;
		private final boolean isAllowedToLand;
		private final eForeignPolicy foreignConflictPolicy;
		
		public PlayerPolicies(String targetLogin, boolean isAllowedToLand, eForeignPolicy foreignConflictPolicy)
		{
			this.targetLogin = targetLogin;
			this.foreignConflictPolicy = foreignConflictPolicy;
			this.isAllowedToLand = isAllowedToLand;
		}
		
		@Override
		public String toString()
		{
			return (isAllowedToLand ? "A" : "Disa") + "llowed to land, " + foreignConflictPolicy.toString();
		}

		public String getTargetLogin()
		{
			return targetLogin;
		}
		
		public eForeignPolicy getForeignPolicy()
		{
			return foreignConflictPolicy;
		}
		
		public boolean isAllowedToLandFleetInHomeTerritory()
		{
			return isAllowedToLand;
		}
	}

	public String getOwnerName()
	{
		return ownerName;
	}

	@Override
	public int getLastObservation()
	{
		return lastObservation;
	}

	@Override
	public boolean isVisible()
	{
		return isVisible;
	}
}
