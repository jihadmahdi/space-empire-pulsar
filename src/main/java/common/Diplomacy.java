package common;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class Diplomacy implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	private final Player owner;
	private final Map<String, PlayerPolicies> policies;
	
	public Diplomacy(Player owner, Map<String,PlayerPolicies> policies)
	{
		this.owner = owner;
		this.policies = policies;
	}
	
	public PlayerPolicies getPolicies(String targetLogin)
	{
		return policies == null ? null : policies.get(targetLogin);
	}
	
	public static class PlayerPolicies implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		private final String targetLogin;
		private final boolean allowedToLandFleetInHomeTerritory;
		private final boolean alwaysEngagedInConflictOnStrangerTerritory;
		
		public PlayerPolicies(String targetLogin, boolean allowFleetAtHome, boolean alwaysEngageFightInStrangerTerritory)
		{
			this.targetLogin = targetLogin;
			this.allowedToLandFleetInHomeTerritory = allowFleetAtHome;
			this.alwaysEngagedInConflictOnStrangerTerritory = alwaysEngageFightInStrangerTerritory;
		}
		
		@Override
		public String toString()
		{
			return (allowedToLandFleetInHomeTerritory?"A":"Disa")+"llow fleet at home, "+(alwaysEngagedInConflictOnStrangerTerritory?"Hostile":"Neutral")+" in stranger territory";
		}

		public String getTargetLogin()
		{
			return targetLogin;
		}

		public boolean isAllowedToLandFleetInHomeTerritory()
		{
			return allowedToLandFleetInHomeTerritory;
		}

		public boolean isAlwaysEngagedInConflictOnStrangerTerritory()
		{
			return alwaysEngagedInConflictOnStrangerTerritory;
		}				
	}

	public Player getOwner()
	{
		return owner;
	}
	
	public String getOwnerName()
	{
		return owner == null ? null : owner.getName();
	}

	public Set<String> targetSet()
	{
		return policies.keySet();
	}
}
