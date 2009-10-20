package common;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Observer;
import java.util.Set;

public class Diplomacy implements Serializable, IObservable
{
	private static final long	serialVersionUID	= 1L;

	/** Is the current area visible for the player. */
	private final boolean isVisible;	
	
	/** Last turn date this area has been visible. */
	private final int lastObservation; 
	
	private final String ownerName;
	private final Map<String, PlayerPolicies> policies;
	
	public Diplomacy(boolean isVisible, int lastObservation, String ownerName, Map<String,PlayerPolicies> policies)
	{
		this.isVisible = isVisible;
		this.lastObservation = lastObservation;
		this.ownerName = ownerName;
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

	public String getOwnerName()
	{
		return ownerName;
	}

	public Set<String> targetSet()
	{
		return policies.keySet();
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
