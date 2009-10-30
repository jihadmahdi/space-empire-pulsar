package org.axan.sep.common;

import java.io.Serializable;
import java.util.Map;

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
		
		@Override
		public boolean equals(Object obj)
		{			
			if (obj == null || !PlayerPolicies.class.isInstance(obj)) return false;
			PlayerPolicies p = PlayerPolicies.class.cast(obj);
			
			return this.isAllowedToLand == p.isAllowedToLand && this.foreignConflictPolicy == p.foreignConflictPolicy && this.targetLogin.equals(p.targetLogin);
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
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !Diplomacy.class.isInstance(obj)) return false;
		Diplomacy dip = Diplomacy.class.cast(obj);
		
		if (!(this.isVisible == dip.isVisible && this.lastObservation == dip.lastObservation && this.ownerName.equals(dip.ownerName))) return false;
		
		return this.playersPolicies.equals(dip.playersPolicies);
	}
	
	@Override
	public String toString()
	{
		return (isVisible?"":"last observation on "+lastObservation+": ")+"["+ownerName+"] "+playersPolicies.toString();
	}
}
